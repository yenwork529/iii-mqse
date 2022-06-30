package org.iii.esd.server.controllers.rest.esd.aaa;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.OrgTree;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.services.OperatingService;
import org.iii.esd.server.utils.ViewUtil;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_AUTHORIZATION_ORG_TREE;
import static org.iii.esd.api.RestConstants.REST_OPERATING_ORG_TREE;
import static org.iii.esd.api.RestConstants.REST_OPERATING_ORG_TREE_ID;
import static org.iii.esd.api.RestConstants.REST_OPERATING_ORG_TREE_LIST;
import static org.iii.esd.api.RestConstants.REST_OPERATING_ORG_TREE_MINE;

@RestController
@Log4j2
@Api(tags = "Authorization",
        description = "使用者授權管理")
public class AuthorizationController {
    @Autowired
    private UserService userService;
    @Autowired
    private OperatingService operatingService;
    @Autowired
    private AuthorizationHelper authorHelper;
    @Autowired
    private QseService qseService;

    @GetMapping(REST_AUTHORIZATION_ORG_TREE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "orgTree",
            notes = "取得轄屬的組織樹")
    public ApiResponse getOrgTree(Authentication authentication) {
        return ViewUtil.get(() -> {
            UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);

            if (Objects.isNull(user)) {
                return new ErrorResponse(Error.emailIsNotFound);
            }

            return operatingService.buildOrgTreeFromUser(user);
        });
    }

    @GetMapping(REST_OPERATING_ORG_TREE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "orgTree",
            notes = "取得所有的組織樹")
    public ApiResponse getTopOrgTree(
            Authentication authentication,
            @RequestParam(name = "date",
                    required = false) String date) {
        return ViewUtil.get(() -> {
            UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
            AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(user);

            if (StringUtils.isEmpty(trimToEmpty(date))) {
                return operatingService.buildTopOrgTreeByQse(userOrg.getQse());
            } else {
                LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return operatingService.buildTopOrgTreeByQseAndDate(userOrg.getQse(), localDate);
            }
        });
    }

    @GetMapping(REST_OPERATING_ORG_TREE_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "orgTree",
            notes = "取得所有的組織樹")
    public ApiResponse getTopOrgTreeList(
            Authentication authentication,
            @RequestParam(name = "date",
                    required = false) String date) {
        return ViewUtil.getAll(() -> {
            if (authorHelper.isSysAuthor(authentication)) {
                if (StringUtils.isEmpty(trimToEmpty(date))) {
                    return getAllQseOrgTreeNow();
                } else {
                    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return getAllQseOrgTree(localDate);
                }
            } else {
                if (StringUtils.isEmpty(trimToEmpty(date))) {
                    UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
                    AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(user);
                    return Collections.singletonList(operatingService.buildTopOrgTreeByQse(userOrg.getQse()));
                } else {
                    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return Collections.singletonList(operatingService.buildTopOrgTreeByDate(localDate));
                }
            }
        });
    }

    @GetMapping(REST_OPERATING_ORG_TREE_ID)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "getOrgTreeByQseId",
            notes = "取得特定 QSE 的組織樹")
    public ApiResponse getTopOrgTreeByQseId(
            Authentication authentication,
            @PathVariable(name = "id") String qseId) {
        return ViewUtil.get(() -> {
            if (authorHelper.isSysAuthor(authentication)) {
                QseProfile qse = qseService.findByQseId(qseId);
                return operatingService.buildTopOrgTreeByQse(qse);
            } else {
                throw new WebException(Error.unauthorized);
            }
        });
    }

    private List<OrgTree> getAllQseOrgTree(LocalDate date) {
        List<QseProfile> qseList = qseService.getQseList();

        return qseList.stream()
                      .map(qse -> operatingService.buildTopOrgTreeByQseAndDate(qse, date))
                      .collect(Collectors.toList());
    }


    @GetMapping(REST_OPERATING_ORG_TREE_MINE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "getOrgTreeOfMine",
            notes = "取得我的組織樹")
    public ApiResponse getOrgTreeOfMine(
            @RequestParam(name = "date",
                    required = false) String date,
            Authentication authentication) {
        return ViewUtil.get(() -> {
            UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
            AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(user);

            OrgTree origOrgTree;
            if (StringUtils.isEmpty(trimToEmpty(date))) {
                origOrgTree = operatingService.buildTopOrgTreeByQse(userOrg.getQse());
            } else {
                LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                origOrgTree = operatingService.buildTopOrgTreeByQseAndDate(userOrg.getQse(), localDate);
            }

            return operatingService.filterOrgTreeByMyAuthor(origOrgTree, user);
        });
    }


    private List<OrgTree> getAllQseOrgTreeNow() {
        List<QseProfile> qseList = qseService.getQseList();

        return qseList.stream()
                      .map(qse -> operatingService.buildTopOrgTreeByQse(qse))
                      .collect(Collectors.toList());
    }
}
