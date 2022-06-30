package org.iii.esd.server.controllers.rest.esd.system;

import javax.annotation.security.RolesAllowed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.thirdparty.service.notify.LineService;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;

@RestController
@Log4j2
public class LineNotifyController {

    @Autowired
    private LineService lineService;
    @Autowired
    private TxgService txgService;

    @PostMapping(value = {"/esd/message/{id}"})
    @RolesAllowed({ROLE_SYSADMIN})
    public ApiResponse sendMessage(
            @PathVariable("id") String txgId,
            @RequestBody LineMessage message) {
        try {
            TxgProfile txg = txgService.getByTxgId(txgId);

            log.info("send to txg {} with line token {}", txg.getTxgId(), txg.getLineToken());

            lineService.sendMessage(txg.getLineToken(), message.getMessage());

            return new SuccessfulResponse();
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(ex.getError(), ex.getParams());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LineMessage {
        private String message;
    }
}
