package org.iii.esd.server.controllers.rest.esd.system;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.SiloUser;
import org.iii.esd.exception.Error;
import org.iii.esd.jwt.security.SecurityConfig;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.service.SiloUserService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.wrap.UserWrapper;
import org.iii.esd.utils.ValidationUtils;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_USER;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_USER_ID;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_USER_SELECT;

@Log4j2
public class SiloUserController extends AbstractRestController {

    private SiloUserService siloUserService;

    public ApiResponse select(HttpServletRequest request, Long roleId, Long companyId, Long srId, Long fieldId) {

        if (checkRolePermission(request, roleId)) {
            return new ErrorResponse(Error.invalidParameter, "roleId");
        }

        if (checkCompanyPermission(request, companyId)) {
            return new ErrorResponse(Error.invalidParameter, "companyId");
        }

        if (checkSrPermission(request, srId)) {
            return new ErrorResponse(Error.invalidParameter, "srId");
        }

        if (checkFieldPermission(request, fieldId)) {
            return new ErrorResponse(Error.invalidParameter, "fieldId");
        }

        List<SiloUser> list = siloUserService.findByExample(companyId, fieldId, null, null, srId).
                                             stream().map(f -> new SiloUser(f)).collect(Collectors.toList());
        return new ListResponse<SiloUser>(list);
    }

    public ApiResponse add(
            SiloUser siloUser,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            String password = siloUser.getPassword();
            if (!StringUtils.isBlank(password) && !ValidationUtils.isPasswordValid(password)) {
                return new ErrorResponse(Error.invalidPassword);
            }
            passwordEncode(siloUser);
            siloUserService.add(UserWrapper.wrap(siloUser));
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

    public ApiResponse get(Long id) {

        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        // TODO add check if not super user only select this userid
        Optional<SiloUserProfile> opt = siloUserService.find(id);
        if (opt.isPresent()) {
            return new DataResponse<SiloUser>(new SiloUser(opt.get()));
        } else {
            return new ErrorResponse(Error.noData, id);
        }
    }

    public ApiResponse update(SiloUser siloUser, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        // TODO add check if not super user only update this userid

        try {
            Long id = siloUser.getId();
            if (id == null) {
                return new ErrorResponse(Error.parameterIsRequired, "id");
            }

            Optional<SiloUserProfile> opt = siloUserService.find(id);
            if (opt.isPresent()) {
                String password = siloUser.getPassword();
                if (!StringUtils.isBlank(password) && !ValidationUtils.isPasswordValid(password)) {
                    return new ErrorResponse(Error.invalidPassword);
                }
                passwordEncode(siloUser);
                siloUserService.update(UserWrapper.merge(opt.get(), siloUser));
            } else {
                return new ErrorResponse(Error.noData, id);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

    public ApiResponse delete(Long id) {

        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        // TODO add check if not super user only delete this userid

        try {
            Optional<SiloUserProfile> opt = siloUserService.find(id);
            if (opt.isPresent()) {
                siloUserService.delete(id);
            } else {
                return new ErrorResponse(Error.noData, id);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

    private void passwordEncode(SiloUser siloUser) {
        if (!StringUtils.isBlank(siloUser.getPassword())) {
            siloUser.setPassword(SecurityConfig.passwordEncoder().encode(siloUser.getPassword()));
        }
    }

}