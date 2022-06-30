package org.iii.esd.server.controllers.rest.esd.organization;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.integrate.Qse;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.jwt.security.UserinfoDetailsService;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.utils.AuthorizationUtil;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.QseWrapper;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_QSE;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_QSE_AVAILABLE;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_QSE_LIST;

@RestController
@Log4j2
@Api(tags = {"QSEProfile", "合格代理商管理"})
public class QseController {

    @Autowired
    private QseService qseService;
    @Autowired
    private UserinfoDetailsService userDetailsService;
    @Autowired
    private AuthorizationHelper authorHelper;

    @GetMapping(REST_ORGANIZATION_QSE_AVAILABLE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get available QSE",
            notes = "取得可用之 QSE",
            response = ApiResponse.class)
    public ApiResponse getAvailableQse(Authentication authentication) {
        return ViewUtil.getAll(() -> {
            if (authorHelper.isSysAuthor(authentication)) {
                return qseService.getQseList();
            } else {
                UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
                AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(user);
                return Collections.singletonList(userOrg.getQse());
            }
        });
    }

    @PostMapping(REST_ORGANIZATION_QSE)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "create QSE",
            notes = "建立QSE",
            response = ApiResponse.class)
    public ApiResponse create(
            @ApiParam(required = true,
                    value = "QseProfile 內容")
            @RequestBody Qse qse) {

        if (Objects.isNull(qse)) {
            return new ErrorResponse(Error.badRequest, "Empty Request Content");
        }

        return ViewUtil.run(() -> qseService.create(QseWrapper.wrap(qse)));
    }

    @PutMapping(REST_ORGANIZATION_QSE)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "update QSE",
            notes = "Update QSE",
            response = ApiResponse.class)
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "QseProfile 內容")
            @RequestBody Qse qse) {

        if (Objects.isNull(qse)) {
            return new ErrorResponse(Error.badRequest, "Empty Request Content");
        }

        return ViewUtil.run(() -> qseService.update(QseWrapper.wrap(qse)));
    }

    @GetMapping({REST_ORGANIZATION_QSE_LIST, REST_ORGANIZATION_QSE})
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get",
            notes = "Get QSE")
    public ApiResponse get(Authentication authentication) {
        List<QseProfile> qseList = qseService.getQseList();

        if (CollectionUtils.isEmpty(qseList)) {
            // return new SuccessfulResponse();
            return new ErrorResponse(Error.notFound);
        } else {
            // return new DataResponse<>(QseWrapper.unwrap(qseList.get(0)));
            /*
            return new ListResponse<>(qseList.stream()
                                             .map(QseWrapper::unwrap)
                                             .collect(Collectors.toList()));
             */
            return ViewUtil.getAll(() -> {
                UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
                Long roleId = user.getRoleId();

                if (AuthorizationUtil.isSysLevel(roleId)) {
                    return qseList.stream()
                                  .map(QseWrapper::unwrap)
                                  .collect(Collectors.toList());
                } else if (AuthorizationUtil.isQseLevel(roleId)) {
                    return qseList.stream()
                                  .filter(qse -> qse.getQseId().equals(user.getOrgId().getId()))
                                  .collect(Collectors.toList());
                } else {
                    throw new WebException(Error.unauthorized);
                }
            });
        }
    }
}
