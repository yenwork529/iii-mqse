package org.iii.esd.server.controllers.rest.esd.system;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.OrgTree;
import org.iii.esd.api.vo.integrate.User;
import org.iii.esd.exception.WebException;
import org.iii.esd.jwt.security.SecurityConfig;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.services.OperatingService;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.UserWrapper;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_USER;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_USER_ID;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_USER_LIST;

@RestController
@Log4j2
@Api(tags = "User",
        description = "使用者")
public class UserController extends AbstractRestController {

    @Autowired
    private UserService userService;
    @Autowired
    private OperatingService operatingService;


    @GetMapping(REST_SYSTEM_USER_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "getAll",
            notes = "查詢使用者")
    public ApiResponse getAll() {
        return ViewUtil.getAll(() -> {
            List<UserProfile> entities = userService.getAll();
            OrgTree orgTree = operatingService.buildTopOrgTree();
            return entities.stream()
                           .map(entity -> UserWrapper.unwrapNew(entity, orgTree))
                           .collect(Collectors.toList());
        });
    }

    @GetMapping(REST_SYSTEM_USER_ID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get",
            notes = "查詢使用者內容")
    public ApiResponse getByEmail(
            @ApiParam(required = true,
                    value = "使用者 id",
                    example = "1")
            @PathVariable("id") Long id) {
        return ViewUtil.get(() -> {
            UserProfile entity = userService.getById(id);
            OrgTree orgTree = operatingService.buildTopOrgTree();
            return UserWrapper.unwrapNew(entity, orgTree);
        });
    }

    @PostMapping(REST_SYSTEM_USER)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "add",
            notes = "新增使用者")
    public ApiResponse create(
            @ApiParam(required = true,
                    value = "使用者內容")
            @RequestBody
            @Valid User user,
            BindingResult bindingResult) {

        passwordEncode(user);

        return ViewUtil.run(() -> {
            UserProfile entity = UserWrapper.wrapNew(user);
            userService.create(entity);
        });
    }

    @PutMapping(REST_SYSTEM_USER)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "update",
            notes = "修改使用者內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "使用者內容")
            @RequestBody
            @Valid User user,
            BindingResult bindingResult) {

        passwordEncode(user);

        return ViewUtil.run(() -> {
            UserProfile entity = UserWrapper.wrapNew(user);
            userService.update(entity);
        });
    }

    private void passwordEncode(User user) {
        if (!StringUtils.isBlank(user.getPassword())) {
            user.setPassword(SecurityConfig.passwordEncoder().encode(user.getPassword()));
        }
    }

}
