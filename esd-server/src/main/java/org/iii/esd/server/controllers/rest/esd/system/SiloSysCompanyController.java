package org.iii.esd.server.controllers.rest.esd.system;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j2;
import org.springframework.validation.BindingResult;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.Payload;
import org.iii.esd.api.vo.SiloCompany;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.service.SiloCompanyProfileService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.wrap.CompanyWrapper;

@Log4j2
public class SiloSysCompanyController extends AbstractRestController {

    private SiloCompanyProfileService companyService;

    public ApiResponse list(HttpServletRequest request) {
        Payload payload = getPayload(request);
        if (checkSysAdmin(payload)) {
            List<SiloCompany> list = companyService.findAll()
                                                   .stream()
                                                   .map(SiloCompany::new)
                                                   .collect(Collectors.toList());
            return new ListResponse<>(list);
        } else {
            return new ErrorResponse(Error.forbidden);
        }
    }

    public ApiResponse get(Long id) {

        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        Optional<SiloCompanyProfile> opt = companyService.find(id);
        if (opt.isPresent()) {
            return new DataResponse<>(new SiloCompany(opt.get()));
        } else {
            return new ErrorResponse(Error.noData, id);
        }
    }

    public ApiResponse add(SiloCompany siloCompany, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            companyService.add(CompanyWrapper.wrap(siloCompany));
            return new SuccessfulResponse();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
    }

    public ApiResponse update(SiloCompany siloCompany, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            Long id = siloCompany.getId();
            if (id == null) {
                return new ErrorResponse(Error.parameterIsRequired, "id");
            }

            Optional<SiloCompanyProfile> opt = companyService.find(id);
            if (opt.isPresent()) {
                companyService.update(CompanyWrapper.merge(opt.get(), siloCompany));
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
