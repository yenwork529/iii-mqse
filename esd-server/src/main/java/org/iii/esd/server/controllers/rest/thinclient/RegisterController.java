package org.iii.esd.server.controllers.rest.thinclient;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.server.api.request.ThinClientRegisterResquest;

import static org.iii.esd.api.RestConstants.REST_THINCLIENT_REGISTER;

@RestController
public class RegisterController {

    @Autowired
    private FieldProfileService fieldProfileService;

    @PostMapping(REST_THINCLIENT_REGISTER)
    public ApiResponse register(HttpServletRequest httpReq, @Valid ThinClientRegisterResquest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ErrorResponse(Error.invalidParameter, bindingResult.getFieldError().getDefaultMessage());
        }

        Optional<FieldProfile> ofieldProfile = fieldProfileService.find(request.getFieldId());
        if (ofieldProfile.isPresent()) {
            FieldProfile fieldProfile = ofieldProfile.get();
            String tcip = fieldProfile.getTcIp();
            if (StringUtils.isBlank(tcip) || httpReq.getRemoteHost().equals(request.getTcip())) {
                if (!EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
                    fieldProfile.setTcEnable(EnableStatus.enable);
                    fieldProfile.setTcIp(httpReq.getRemoteHost());
                    fieldProfileService.update(fieldProfile);
                }
            } else {
                return new ErrorResponse(Error.thinclientIPisUnmatch);
            }
        } else {
            return new ErrorResponse(Error.invalidFieldId);
        }
        return new SuccessfulResponse();
    }

}