package org.iii.esd.server.controllers.rest.esd.organization;

import java.util.Collections;
import java.util.Objects;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.integrate.Txg;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.jwt.security.UserinfoDetailsService;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.TxgProfileWrapper;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_TXG;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_TXG_ID;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_TXG_LIST;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_TXG_LIST_BY_QSE;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_TXG_LIST_CURR;

@RestController
@Log4j2
@Api(tags = "交易群組管理")
public class TxgController extends AbstractRestController {

    @Autowired
    private TxgService txgService;
    @Autowired
    private AuthorizationHelper authorHelper;
    @Autowired
    private UserinfoDetailsService userinfoDetailsService;

    @GetMapping(REST_ORGANIZATION_TXG_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "list",
            notes = "取得即時備轉列表")
    public ApiResponse list(Authentication authentication) {
        return ViewUtil.getAll(() -> {
            if (authorHelper.isSysAuthor(authentication)) {
                return txgService.getAll();
            } else {
                UserProfile userProfile = authorHelper.getUserProfileByAuthentication(authentication);
                AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(userProfile);
                return txgService.findByQseId(userOrg.getQse().getQseId());
            }
        });
    }

    @GetMapping(REST_ORGANIZATION_TXG_LIST_CURR)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "listCurrent",
            notes = "取得當前所屬 QSE 之交易群組列表")
    public ApiResponse listCurr(Authentication authentication) {
        return ViewUtil.getAll(() -> {
            UserProfile userProfile = authorHelper.getUserProfileByAuthentication(authentication);
            AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(userProfile);
            return txgService.findByQseId(userOrg.getQse().getQseId());
        });
    }

    @GetMapping(REST_ORGANIZATION_TXG_LIST_BY_QSE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "listByQSE",
            notes = "取得當前所屬 QSE 之交易群組列表，Admin 可換 QSE ID")
    public ApiResponse listByQse(
            @PathVariable("id") String qseId,
            Authentication authentication) {
        return ViewUtil.getAll(() -> {
            if (authorHelper.isSysAuthor(authentication) && StringUtils.isNotEmpty(qseId)) {
                return txgService.findByQseId(qseId);
            } else if (authorHelper.isQseAuthor(authentication)) {
                UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
                return txgService.findByQseId(user.getOrgId().getId());
            } else if (authorHelper.isTxgAuthor(authentication)) {
                UserProfile user = authorHelper.getUserProfileByAuthentication(authentication);
                AuthorizationHelper.UserOrg userOrg = authorHelper.getUserOrg(user);
                return Collections.singletonList(txgService.findByTxgId(userOrg.getTxg().getTxgId()));
            } else {
                throw new WebException(Error.unauthorized);
            }
        });
    }

    @GetMapping(REST_ORGANIZATION_TXG_ID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get",
            notes = "查詢即時備轉內容")
    public ApiResponse get(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId) {
        if (StringUtils.isEmpty(txgId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.get(() -> {
            TxgProfile txg = txgService.getByTxgId(txgId);
            return TxgProfileWrapper.unwrap(txg);
        });
    }

    @PostMapping(REST_ORGANIZATION_TXG)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "create",
            notes = "新增即時備轉內容")
    public ApiResponse add(
            @ApiParam(required = true,
                    value = "即時備轉內容")
            @RequestBody
            @Valid Txg txg,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        return ViewUtil.run(() -> {
            TxgProfile txgProfile = TxgProfileWrapper.wrap(txg);
            txgService.create(txgProfile);
        });
    }

    @PutMapping(REST_ORGANIZATION_TXG_ID)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "update",
            notes = "修改即時備轉內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入即時備轉內容")
            @RequestBody
            @Valid Txg txg,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        if (StringUtils.isEmpty(txgId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.run(() -> {
            TxgProfile txgProfile = txgService.getByTxgId(txgId);
            TxgProfileWrapper.copy(txg, txgProfile);
            txgService.update(txgProfile);
        });
    }
}