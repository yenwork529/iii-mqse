package org.iii.esd.server.controllers.rest.esd.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.Device;
import org.iii.esd.api.vo.Payload;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.wrap.DeviceWrapper;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_DEVICE;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_DEVICE_ID;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_DEVICE_LIST;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_DEVICE_SELECT;

// @RestController
@Log4j2
// @Api(tags = "Device", description = "設備")
public class SiloDeviceController extends AbstractRestController {

    // @Autowired
    private DeviceService deviceService;

    // @Autowired
    private FieldProfileService fieldProfileService;

    // @GetMapping(REST_SYSTEM_DEVICE_LIST)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER, ROLE_FIELDADMIN, ROLE_FIELDUSER})
    // @ApiOperation(value = "DeviceList", notes = "取得設備列表")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "請傳入fieldId",
                    example = "1") @RequestParam(value = "fieldId",
                    required = false) Long fieldId) {

        if (fieldId == null || fieldId < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "fieldId");
        }

        if (checkFieldPermission(request, fieldId)) {
            return new ErrorResponse(Error.invalidParameter, "fieldId");
        }

        List<DeviceProfile> list = deviceService.findDeviceProfileByFieldId(fieldId);
        return new ListResponse<DeviceProfile>(list);
    }

    // @GetMapping(REST_SYSTEM_DEVICE_SELECT)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN})
    // @ApiOperation(value = "select", notes = "依據條件篩選設備列表")
    public ApiResponse select(HttpServletRequest request,
            @ApiParam(required = false,
                    value = "請傳入companyId",
                    example = "1") @RequestParam(value = "companyId",
                    required = false) Long companyId,
            @ApiParam(required = false,
                    value = "請傳入srId",
                    example = "1") @RequestParam(value = "srId",
                    required = false) Long srId,
            @ApiParam(required = false,
                    value = "請傳入fieldId",
                    example = "1") @RequestParam(value = "fieldId",
                    required = false) Long fieldId
    ) {
        Payload payload = getPayload(request);
        log.debug(payload);

        if (checkCompanyPermission(request, companyId)) {
            return new ErrorResponse(Error.invalidParameter, "companyId");
        }

        if (checkSrPermission(request, srId)) {
            return new ErrorResponse(Error.invalidParameter, "srId");
        }

        if (checkFieldPermission(request, fieldId)) {
            return new ErrorResponse(Error.invalidParameter, "fieldId");
        }

        if (payload.getRoles().contains(Integer.valueOf(ROLE_SIADMIN))) {
            companyId = payload.getSiloCompanyId();
            srId = payload.getSrId();
        }

        Set<Long> fieldIds = fieldProfileService.findByExample(companyId, null, null, srId, fieldId, null).
                stream().map(FieldProfile::getId).collect(Collectors.toSet());

        List<Device> list = new ArrayList<>();
        if (fieldIds != null && fieldIds.size() > 0) {
            list = deviceService.findDeviceProfileByFieldId(fieldIds).
                    stream().map(f -> new Device(f)).collect(Collectors.toList());
        }
        return new ListResponse<Device>(list);
    }

    // @GetMapping(REST_SYSTEM_DEVICE_ID)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN})
    // @ApiOperation(value = "get", notes = "查詢設備內容")
    public ApiResponse get(@ApiParam(required = true,
            value = "請傳入id",
            example = "1") @PathVariable("id") String id) {

        if (id == null) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        Optional<DeviceProfile> opt = deviceService.findDeviceProfileById(id);
        if (opt.isPresent()) {
            return new DataResponse<Device>(new Device(opt.get()));
        } else {
            return new ErrorResponse(Error.noData, id);
        }
    }

    // @PutMapping(REST_SYSTEM_DEVICE)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN})
    // @ApiOperation(value = "update", notes = "修改設備內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "請傳入設備內容") @RequestBody @Valid Device device,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            String id = device.getId();
            if (id == null) {
                return new ErrorResponse(Error.parameterIsRequired, "id");
            }

            Optional<DeviceProfile> opt = deviceService.findDeviceProfileById(id);
            if (opt.isPresent()) {
                deviceService.saveDeviceProfile(DeviceWrapper.merge(opt.get(), device));
            } else {
                return new ErrorResponse(Error.noData, id);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

}