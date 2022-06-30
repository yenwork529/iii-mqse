package org.iii.esd.server.controllers.rest.esd.main;

import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.services.MainBoardService;
import org.iii.esd.server.utils.AuthenticationUtil;
import org.iii.esd.server.utils.ViewUtil;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_MAINBOARD;
import static org.iii.esd.api.RestConstants.REST_MAINBOARD_ID;

@RestController
@Log4j2
@Api(tags = "MainBoard",
        description = "服務總覽")
public class MainBoardController {

    @Autowired
    private UserService userService;
    @Autowired
    private MainBoardService mainBoardService;
    @Autowired
    private AuthorizationHelper authorHelper;

    @GetMapping(REST_MAINBOARD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "getMainBoard",
            notes = "取得服務總覽資料")
    public ApiResponse getMainBoard(Authentication authentication) {
        return ViewUtil.getAll(() -> {
            if (authorHelper.isResAuthor(authentication)) {
                throw new WebException(Error.unauthorized);
            } else {
                UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
                return mainBoardService.buildMainBoardItemsFromUser(user);
            }
        });
    }

    @GetMapping(REST_MAINBOARD_ID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "getMainBoardByQseId",
            notes = "依 qseId 取得服務總覽資料")
    public ApiResponse getMainBoardByQseId(
            @PathVariable("id") String qseId,
            Authentication authentication) {
        return ViewUtil.getAll(()->{
            if (authorHelper.isResAuthor(authentication)) {
                throw new WebException(Error.unauthorized);
            } else {
                return mainBoardService.buildMainBoardItemsFromQseId(qseId);
            }
        });
    }
}
