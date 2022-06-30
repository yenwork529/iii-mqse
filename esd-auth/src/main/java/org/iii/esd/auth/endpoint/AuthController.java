package org.iii.esd.auth.endpoint;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.iii.esd.Constants;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.auth.vo.JwtResponse;
import org.iii.esd.auth.vo.SignInRequest;
import org.iii.esd.auth.vo.SignUpRequest;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.jwt.security.TokenProvider;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.thirdparty.service.notify.MailService;
import org.iii.esd.utils.MathUtils;

@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private MailService mailService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @Valid SignInRequest signInRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.error(bindingResult.getFieldError().getDefaultMessage());
            bindingResult.getAllErrors().forEach(e -> log.error(e.getDefaultMessage()));
            return ResponseEntity.ok(new ErrorResponse(Error.incorrectEmailOrPassward));
        } else {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);
            JwtResponse jwt = new JwtResponse(token);
            jwt.setExpire(tokenProvider.getExpirationFromToken(token).getTime());
            return ResponseEntity.ok(jwt);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<? extends ApiResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.ok(new ErrorResponse(Error.incorrectEmailOrPassward));
        }
        if (userService.exists(signUpRequest.getEmail())) {
            return new ResponseEntity<>(
                    new ErrorResponse(Error.emailAddressAlreadyExists),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        UserProfile userProfile = UserProfile.builder()
                                             .name(signUpRequest.getUsername())
                                             .email(signUpRequest.getEmail())
                                             .password(passwordEncoder.encode(signUpRequest.getPassword()))
                                             .companyId(signUpRequest.getCompanyId())
                                             .orgId(buildOrgId(signUpRequest))
                                             .roleId(signUpRequest.getRoleids()
                                                                  .stream()
                                                                  .findFirst()
                                                                  .orElseThrow())
                                             .build();
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                                                  .path("/api/users/{username}")
                                                  .buildAndExpand(userProfile.getEmail())
                                                  .toUri();

        userService.create(userProfile);
        log.info(signUpRequest.getUsername());
        return ResponseEntity.created(location).body(new SuccessfulResponse());
    }

    private UserProfile.OrgId buildOrgId(SignUpRequest signUpRequest) {
        Long roleId = signUpRequest.getRoleids()
                                   .stream()
                                   .findFirst()
                                   .orElseThrow();
        switch (String.valueOf(roleId)) {
            case Constants.ROLE_SYSADMIN:
                return UserProfile.OrgId.builder()
                                        .type(UserProfile.OrgType.QSE)
                                        .id(signUpRequest.getQseId())
                                        .build();
            case Constants.ROLE_SIADMIN:
            case Constants.ROLE_SIUSER:
                return UserProfile.OrgId.builder()
                                        .type(UserProfile.OrgType.TXG)
                                        .id(signUpRequest.getTxgId())
                                        .build();
            case Constants.ROLE_FIELDADMIN:
            case Constants.ROLE_FIELDUSER:
            default:
                return UserProfile.OrgId.builder()
                                        .type(UserProfile.OrgType.RES)
                                        .id(signUpRequest.getResId())
                                        .build();
        }
    }

    @PostMapping("/forget")
    public ApiResponse forgetPassword(
            HttpServletRequest request,
            @Valid SignInRequest signInRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.emailFormatInvalid);
        }

        UserProfile userProfile = userService.findByEmail(signInRequest.getUsername());
        if (userProfile == null) {
            return new ErrorResponse(Error.emailIsNotFound);
        } else {
            String reset = MathUtils.random(20);
            try {
                Map<String, Object> model = new HashMap<>();
                model.put("name", userProfile.getName());
                model.put("url",
                        String.format("https://%s:57000/auth/active?u=%d&k=%s", request.getLocalAddr(), userProfile.getId(), reset));
                mailService.sendMailByFtl(userProfile.getEmail(), "調度系統密碼重置通知", "reset.ftl", model);
            } catch (Exception e) {
                log.error("mail server is failed, {}", e.getMessage());
                return new ErrorResponse(Error.internalServerError);
            }

            userProfile.setPassword(passwordEncoder.encode(signInRequest.getPassword()));
            userProfile.setEnableStatus(EnableStatus.disable);
            userProfile.setReset(reset);

            userService.update(userProfile);

            return new SuccessfulResponse();
        }
    }

    @PostMapping("/active")
    public ApiResponse active(
            @RequestParam(value = "u",
                    required = true) Long id,
            @RequestParam(value = "k",
                    required = true) String resetkey
    ) {

        if (id == null || resetkey == null) {
            return new ErrorResponse(Error.parameterIsRequired);
        }

        UserProfile userProfile = userService.findById(id).orElse(null);
        if (userProfile == null) {
            return new ErrorResponse(Error.emailIsNotFound);
        } else {
            if (resetkey.equals(userProfile.getReset())) {
                userProfile.setEnableStatus(EnableStatus.enable);
                userProfile.setRetry(0);
                userService.update(userProfile);
                return new SuccessfulResponse();
            } else {
                return new ErrorResponse(Error.invalidParameter, "k");
            }
        }
    }
}