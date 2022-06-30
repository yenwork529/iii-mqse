package org.iii.esd.server.controllers.rest.esd.organization;

import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.Device;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.ConnectionService;
import org.iii.esd.mongo.service.integrate.TxgDeviceService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.DeviceWrapper;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_DEVICE;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_DEVICE_ID;
import static org.iii.esd.api.RestConstants.REST_ORGANIZATION_DEVICE_LIST;

@RestController
@Log4j2
@Api(tags = "Device",
        description = "設備")
public class DevController extends AbstractRestController {

    @Autowired
    private TxgDeviceService deviceService;

    @Autowired
    private TxgFieldService resService;

    @Autowired
    private AuthorizationHelper authorHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connService;

    @GetMapping(REST_ORGANIZATION_DEVICE_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "DeviceList",
            notes = "取得設備列表")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "fieldId",
                    example = "RES-0000-01")
            @RequestParam(value = "fieldId",
                    required = false) String resId,
            Authentication authentication) {

        if (StringUtils.isEmpty(resId)) {
            return ViewUtil.getAll(() -> {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String email = userDetails.getUsername();
                UserProfile user = userService.findByEmail(email);

                List<TxgDeviceProfile> list = deviceService.getByUser(user);

                for (TxgDeviceProfile dev : list) {
                    Long ticks = connService.getLastTicks(dev.getId());
                    if (!Objects.isNull(ticks)) {
                        Date reportTime = new Date(ticks);
                        log.info("set report time of {}", dev.getId());
                        dev.setReportTime(reportTime);
                    } else {
                        log.info("no report time of {}", dev.getId());
                    }
                }

                return list.stream()
                           .map(DeviceWrapper::unwrapNew)
                           .collect(Collectors.toList());
            });
        } else {
            if (!resService.isValidResId(resId)) {
                return new ErrorResponse(Error.parameterIsRequired, "fieldId");
            }

            return ViewUtil.getAll(() -> {
                authorHelper.checkResAuthorization(authentication, resId);

                List<TxgDeviceProfile> list = deviceService.findByResId(resId);

                for (TxgDeviceProfile dev : list) {
                    Long ticks = connService.getLastTicks(dev.getId());
                    if (!Objects.isNull(ticks)) {
                        Date reportTime = new Date(ticks);
                        log.info("set report time of {}", dev.getId());
                        dev.setReportTime(reportTime);
                    } else {
                        log.info("no report time of {}", dev.getId());
                    }
                }

                return list.stream()
                           .map(DeviceWrapper::unwrapNew)
                           .collect(Collectors.toList());
            });
        }
    }

    @GetMapping(REST_ORGANIZATION_DEVICE_ID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get",
            notes = "查詢設備內容")
    public ApiResponse get(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String id) {

        if (StringUtils.isEmpty(id)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.get(() -> DeviceWrapper.unwrapNew(deviceService.getDeviceById(id)));
    }

    @PostMapping(REST_ORGANIZATION_DEVICE)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "create",
            notes = "新增設備內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "請傳入設備內容")
            @RequestBody @Valid Device device,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        return ViewUtil.run(() -> {
            TxgDeviceProfile entity = DeviceWrapper.wrapNew(device);
            deviceService.create(entity);
        });
    }

    @PutMapping(REST_ORGANIZATION_DEVICE_ID)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "update",
            notes = "修改設備內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String deviceId,
            @ApiParam(required = true,
                    value = "請傳入設備內容")
            @RequestBody
            @Valid Device device,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        if (StringUtils.isEmpty(deviceId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.run(() -> {
            TxgDeviceProfile entity = deviceService.getDeviceById(deviceId);
            DeviceWrapper.copy(device, entity);
            deviceService.update(entity);
        });
    }

}