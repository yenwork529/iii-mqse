package org.iii.esd.server.controllers.rest.esd.organization;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.swing.text.View;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.integrate.Res;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.TxgFieldProfileWrapper;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_RES;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_RES_ID;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_RES_LIST;

@RestController
@Log4j2
@Api(tags = "場域資源管理")
public class ResController extends AbstractRestController {

    @Autowired
    private TxgFieldService txgFieldService;

    @GetMapping(REST_ORGANIZATION_RES_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "list",
            notes = "取得場域資源列表")
    public ApiResponse list(
            @RequestParam(name = "txgId") String txgId) {
        List<TxgFieldProfile> resList;

        if (StringUtils.isEmpty(txgId)) {
            resList = txgFieldService.getAll();
        } else {
            resList = txgFieldService.findByTxgId(txgId);
        }

        return new ListResponse<>(resList.stream()
                                         .map(TxgFieldProfileWrapper::unwrap)
                                         .collect(Collectors.toList()));
    }

    @GetMapping(REST_ORGANIZATION_RES_ID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get",
            notes = "查詢場域資源內容")
    public ApiResponse get(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String resId) {
        if (StringUtils.isEmpty(resId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.get(()->{
            TxgFieldProfile res = txgFieldService.getByResId(resId);
            return TxgFieldProfileWrapper.unwrap(res);
        });
    }

    @PostMapping(REST_ORGANIZATION_RES)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "create",
            notes = "新增場域資源內容")
    public ApiResponse add(
            @ApiParam(required = true,
                    value = "場域資源內容")
            @RequestBody
            @Valid Res res,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        return ViewUtil.run(() -> {
            TxgFieldProfile resProfile = TxgFieldProfileWrapper.wrap(res);
            txgFieldService.create(resProfile);
        });
    }

    @PutMapping(REST_ORGANIZATION_RES_ID)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "update",
            notes = "修改場域資源內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String resId,
            @ApiParam(required = true,
                    value = "請傳入場域資源內容")
            @RequestBody
            @Valid Res res,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        if (StringUtils.isEmpty(resId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.run(()->{
            TxgFieldProfile resProfile = txgFieldService.getByResId(resId);
            TxgFieldProfileWrapper.copy(res, resProfile);
            txgFieldService.update(resProfile);
        });
    }
}
