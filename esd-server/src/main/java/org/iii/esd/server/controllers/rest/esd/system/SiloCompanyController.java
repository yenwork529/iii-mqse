package org.iii.esd.server.controllers.rest.esd.system;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j2;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.vo.Payload;
import org.iii.esd.api.vo.SiloCompany;
import org.iii.esd.mongo.service.SiloCompanyProfileService;
import org.iii.esd.server.controllers.rest.AbstractRestController;

@Log4j2
public class SiloCompanyController extends AbstractRestController {

    private SiloCompanyProfileService siloCompanyProfileService;

    public ApiResponse list(HttpServletRequest request) {

        Payload payload = getPayload(request);
        Long companyId = checkSysAdmin(payload) ? null : payload.getSiloCompanyId();

        List<SiloCompany> list = siloCompanyProfileService.findCompanyProfileByExample(companyId).
                                                          stream().map(com -> new SiloCompany(com)).collect(Collectors.toList());
        log.debug(list);
        return new ListResponse<SiloCompany>(list);
    }

}