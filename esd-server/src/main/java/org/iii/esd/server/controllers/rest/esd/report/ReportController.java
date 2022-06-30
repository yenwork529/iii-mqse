package org.iii.esd.server.controllers.rest.esd.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.controllers.rest.esd.history.AfcHelper;
import org.iii.esd.server.services.ReportService;
import org.iii.esd.server.utils.ViewUtil;

import static org.iii.esd.Constants.ROLE_AFCADMIN;
import static org.iii.esd.Constants.ROLE_AFCUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_REPORT_DOWNLOAD;

@RestController
@Log4j2
@Api(tags = "ReportController",
        description = "報表處理")
public class ReportController {

    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private QseService qseService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private AfcHelper afcHelper;
    @Autowired
    private AuthorizationHelper authorHelper;
    @Autowired
    private ReportService service;

    @GetMapping(REST_REPORT_DOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_AFCADMIN, ROLE_AFCUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ResponseEntity<?> download(
            @PathVariable("qseId") String qseId,
            @PathVariable("txgId") String txgId,
            @PathVariable("resId") String resId,
            @RequestParam(value = "begin",
                    required = false) String begin,
            @RequestParam(value = "end",
                    required = false) String end,
            @RequestParam(value = "type",
                    required = false) String type,
            Authentication authentication) {
        log.info("handle report download:");
        log.info("qseId: {}", qseId);
        log.info("txgId: {}", txgId);
        log.info("resId: {}", resId);
        log.info("begin: {}", begin);
        log.info("end: {}", end);

        String email = authorHelper.getUserNameByAuthentication(authentication);

        if (service.isDownloading(email)) {
            return ResponseEntity.ok()
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(new SuccessfulResponse());
        }

        LocalDateTime queryStart = processQueryDate(begin);
        LocalDateTime queryEnd = processQueryDate(end);

        return ViewUtil.getFile(() ->
                service.prepareFile(email, qseId, txgId, resId, queryStart, queryEnd), MediaType.APPLICATION_OCTET_STREAM);
    }

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private LocalDateTime processQueryDate(String dateStr) {
        return LocalDateTime.parse(dateStr, INPUT_FORMATTER);
    }
}
