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
import org.iii.esd.api.vo.AutomaticFrequencyControl;
import org.iii.esd.api.vo.Payload;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.wrap.AutomaticFrequencyControlWrapper;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_AFC;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_AFC_ID;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_AFC_LIST;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_AFC_SELECT;

// @RestController
@Log4j2
// @Api(tags = "AutomaticFrequencyControl", description = "調頻備轉管理")
public class SysAutomaticFrequencyControlController extends AbstractRestController {

    // @Autowired
    private AutomaticFrequencyControlService afcService;

    // @GetMapping(REST_SYSTEM_AFC_LIST)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER, ROLE_QSEUSER, ROLE_QSEADMIN})
    // @ApiOperation(value = "list", notes = "取得調頻備轉列表")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "請傳入啟用狀態") @RequestParam(value = "enableStatus",
                    required = false) EnableStatus enableStatus
    ) {
        Payload payload = getPayload(request);
        Long companyId = checkSysAdmin(payload) ? null : payload.getSiloCompanyId();
        List<AutomaticFrequencyControl> list =
                afcService.findAutomaticFrequencyControlProfileByCompanyIdAndEnableStatus(companyId, enableStatus).
                        stream().map(sr -> new AutomaticFrequencyControl(sr)).collect(Collectors.toList());
        return new ListResponse<AutomaticFrequencyControl>(list);
    }

    // @GetMapping(REST_SYSTEM_AFC_SELECT)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_QSEADMIN})
    // @ApiOperation(value = "select", notes = "依據條件篩調頻備轉列表")
    public ApiResponse select(HttpServletRequest request,
            @ApiParam(required = false,
                    value = "請傳入companyId",
                    example = "1") @RequestParam(value = "companyId",
                    required = false) Long companyId,
            @ApiParam(required = true,
                    value = "請傳入啟用狀態") @RequestParam(value = "enableStatus",
                    required = false) EnableStatus enableStatus
    ) {

        if (checkCompanyPermission(request, companyId)) {
            return new ErrorResponse(Error.invalidParameter, "companyId");
        }
        List<AutomaticFrequencyControl> list =
                afcService.findAutomaticFrequencyControlProfileByCompanyIdAndEnableStatus(companyId, enableStatus).
                        stream().map(sr -> new AutomaticFrequencyControl(sr)).collect(Collectors.toList());
        return new ListResponse<AutomaticFrequencyControl>(list);
    }

    // @PostMapping(REST_SYSTEM_AFC)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_QSEADMIN})
    // @ApiOperation(value = "add", notes = "新增調頻備轉內容")
    public ApiResponse add(
            @ApiParam(required = true,
                    value = "請傳入調頻備轉內容") @RequestBody @Valid AutomaticFrequencyControl automaticFrequencyControl,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            afcService.addAutomaticFrequencyControlProfile(AutomaticFrequencyControlWrapper.wrap(automaticFrequencyControl));
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

    // @PutMapping(REST_SYSTEM_AFC)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_QSEADMIN})
    // @ApiOperation(value = "update", notes = "修改調頻備轉內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "請傳入調頻備轉內容") @RequestBody @Valid AutomaticFrequencyControl automaticFrequencyControl,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            Long id = automaticFrequencyControl.getId();
            if (id == null) {
                return new ErrorResponse(Error.parameterIsRequired, "id");
            }

            Optional<AutomaticFrequencyControlProfile> opt = afcService.findAutomaticFrequencyControlProfile(id);
            if (opt.isPresent()) {
                afcService.updateAutomaticFrequencyControlProfile(
                        AutomaticFrequencyControlWrapper.merge(opt.get(), automaticFrequencyControl));
            } else {
                return new ErrorResponse(Error.noData, id);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

    // @GetMapping(REST_SYSTEM_AFC_ID)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER, ROLE_QSEUSER, ROLE_QSEADMIN})
    // @ApiOperation(value = "get", notes = "查詢調頻備轉內容")
    public ApiResponse get(@ApiParam(required = true,
            value = "請傳入id",
            example = "1") @PathVariable("id") Long id) {

        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        Optional<AutomaticFrequencyControlProfile> opt = afcService.findAutomaticFrequencyControlProfile(id);
        if (opt.isPresent()) {
            return new DataResponse<AutomaticFrequencyControl>(new AutomaticFrequencyControl(opt.get()));
        } else {
            return new ErrorResponse(Error.noData, id);
        }
    }

}