package org.iii.esd.server.controllers.rest.esd.system;

import java.util.List;
import java.util.Objects;
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
import org.iii.esd.api.vo.integrate.Company;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.CompanyProfile;
import org.iii.esd.mongo.service.integrate.CompanyService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.CompanyWrapper;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_COMPANY;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_COMPANY_ID;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_COMPANY_LIST;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_COM_LIST;

@RestController
@Log4j2
@Api(tags = "Company",
        description = "公司資訊維護")
public class CompanyController extends AbstractRestController {

    @Autowired
    private CompanyService companyService;

    @GetMapping(value = {REST_SYSTEM_COMPANY_LIST, REST_SYSTEM_COM_LIST})
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "list",
            notes = "取得公司列表")
    public ApiResponse list() {
        List<CompanyProfile> companyProfiles = companyService.getAll();
        return new ListResponse<>(companyProfiles);
    }

    @GetMapping(REST_SYSTEM_COMPANY_ID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "get",
            notes = "查詢公司內容")
    public ApiResponse get(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String companyId) {
        if (StringUtils.isEmpty(companyId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        try {
            CompanyProfile entity = companyService.getByCompanyId(companyId);
            return new DataResponse<>(CompanyWrapper.unwrapNew(entity));
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(ex.getError());
        }
    }

    @PostMapping(REST_SYSTEM_COMPANY)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "add",
            notes = "新增公司內容")
    public ApiResponse add(
            @ApiParam(required = true,
                    value = "公司資料內容")
            @RequestBody
            @Valid Company company,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        String companyId = company.getCompanyId();
        if (StringUtils.isEmpty(companyId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.run(() -> companyService.create(CompanyWrapper.wrapNew(company)));
    }

    @PutMapping(REST_SYSTEM_COMPANY)
    @RolesAllowed({ROLE_SYSADMIN})
    @ApiOperation(value = "update",
            notes = "修改公司內容")
    public ApiResponse update(
            @ApiParam(required = true,
                    value = "公司資料內容")
            @RequestBody
            @Valid Company company,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        String companyId = company.getCompanyId();
        if (StringUtils.isEmpty(companyId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        return ViewUtil.run(() -> companyService.update(CompanyWrapper.wrapNew(company)));
    }
}
