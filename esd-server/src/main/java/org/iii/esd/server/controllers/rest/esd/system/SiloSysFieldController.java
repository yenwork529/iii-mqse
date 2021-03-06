package org.iii.esd.server.controllers.rest.esd.system;

import java.util.ArrayList;
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
import org.iii.esd.api.vo.Field;
import org.iii.esd.api.vo.Payload;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.wrap.FieldWrapper;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_FIELD;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_FIELD_ID;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_FIELD_LIST;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_FIELD_SELECT;

// @RestController
@Log4j2
// @Api(tags = "Field", description = "??????")
public class SiloSysFieldController extends AbstractRestController {

    // @Autowired
    private FieldProfileService fieldProfileService;

    // @GetMapping(REST_SYSTEM_FIELD_LIST)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER, ROLE_FIELDADMIN, ROLE_FIELDUSER, ROLE_QSEUSER, ROLE_QSEADMIN})
    // @ApiOperation(value = "list", notes = "??????????????????")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "?????????????????????") @RequestParam(value = "enableStatus",
                    required = false) EnableStatus enableStatus
    ) {
        Payload payload = getPayload(request);
        log.debug(payload);
        Long companyId = checkSysAdmin(payload) ? null : payload.getSiloCompanyId();
        Long fieldId = payload.getSiloFieldId();

        List<Field> list = new ArrayList<>();
        if (fieldId == null) {
            list = fieldProfileService.findFieldProfileByCompanyId(companyId, enableStatus).
                    stream().map(f -> new Field(f)).collect(Collectors.toList());
        } else {
            Optional<FieldProfile> oFieldProfile = fieldProfileService.find(fieldId);
            if (!oFieldProfile.isPresent()) {
                return new ErrorResponse(Error.invalidFieldId);
            } else {
                FieldProfile fieldProfile = oFieldProfile.get();
                if (EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
                    return new ErrorResponse(Error.isNotEnabled, fieldId);
                } else {
                    list.add(new Field(fieldProfile));
                }
            }
        }
        return new ListResponse<Field>(list);
    }

    // @GetMapping(REST_SYSTEM_FIELD_SELECT)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER, ROLE_QSEUSER, ROLE_QSEADMIN})
    // @ApiOperation(value = "select", notes = "??????????????????????????????")
    public ApiResponse select(HttpServletRequest request,
            @ApiParam(required = false,
                    value = "?????????companyId",
                    example = "1") @RequestParam(value = "companyId",
                    required = false) Long companyId,
            @ApiParam(required = false,
                    value = "?????????srId",
                    example = "1") @RequestParam(value = "srId",
                    required = false) Long srId,
            @ApiParam(required = false,
                    value = "?????????????????????",
                    example = "enable") @RequestParam(value = "enableStatus",
                    required = false) EnableStatus enableStatus
    ) {

        if (checkCompanyPermission(request, companyId)) {
            return new ErrorResponse(Error.invalidParameter, "companyId");
        }

        if (checkSrPermission(request, srId)) {
            return new ErrorResponse(Error.invalidParameter, "srId");
        }

        Payload payload = getPayload(request);
        if (companyId == null) {
            companyId = checkSysAdmin(payload) ? null : payload.getSiloCompanyId();
        }

        if (srId == null) {
            srId = checkSysAdmin(payload) ? null : payload.getSrId();
        }

        List<Field> list = fieldProfileService.findByExample(companyId, null, null, srId, enableStatus).
                stream().map(f -> new Field(f)).collect(Collectors.toList());
        return new ListResponse<Field>(list);
    }

    // @PostMapping(REST_SYSTEM_FIELD)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_QSEADMIN})
    // @ApiOperation(value = "add", notes = "????????????")
    public ApiResponse add(
            @ApiParam(required = true,
                    value = "?????????????????????") @RequestBody @Valid Field field,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            fieldProfileService.add(FieldWrapper.wrap(field));
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        return new SuccessfulResponse();
    }

    // @GetMapping(REST_SYSTEM_FIELD_ID)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER, ROLE_QSEUSER, ROLE_QSEADMIN})
    // @ApiOperation(value = "get", notes = "??????????????????")
    public ApiResponse get(@ApiParam(required = true,
            value = "?????????id",
            example = "1") @PathVariable("id") Long id) {

        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        Optional<FieldProfile> opt = fieldProfileService.find(id);
        if (opt.isPresent()) {
            return new DataResponse<Field>(new Field(opt.get()));
        } else {
            return new ErrorResponse(Error.noData, id);
        }
    }

    // @PutMapping(REST_SYSTEM_FIELD)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_QSEADMIN})
    // @ApiOperation(value = "update", notes = "??????????????????")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "?????????????????????") @RequestBody @Valid Field field,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            Long id = field.getId();
            if (id == null) {
                return new ErrorResponse(Error.parameterIsRequired, "id");
            }

            Optional<FieldProfile> opt = fieldProfileService.find(id);
            if (opt.isPresent()) {
                fieldProfileService.update(FieldWrapper.merge(opt.get(), field));
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