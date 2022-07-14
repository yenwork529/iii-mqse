package org.iii.esd.server.controllers.rest.esd.report;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

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
import static org.iii.esd.api.RestConstants.REST_REPORT_ENERGYDOWNLOAD;

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
		service.prepareFile(email, qseId, txgId, resId, queryStart, queryEnd, false),
				MediaType.APPLICATION_OCTET_STREAM);
	 }
    
    
    @GetMapping(REST_REPORT_ENERGYDOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_AFCADMIN, ROLE_AFCUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ResponseEntity<?> energyDownload(
            @PathVariable("qseId") String qseId,
            @PathVariable("txgId") String txgId,
            @PathVariable("resId") String resId,
            @RequestParam(value = "dataMonth",
                    required = false) String dataMonth,
            Authentication authentication) {
        log.info("handle energy report download:");
        log.info("qseId: {}", qseId);
        log.info("txgId: {}", txgId);
        log.info("resId: {}", resId);
        log.info("dataMonth: {}", dataMonth);

        String email = authorHelper.getUserNameByAuthentication(authentication);
		
		if (service.isDownloading(email)) {
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new SuccessfulResponse());
		}
		String startDateTime = dataMonth + "-01 00:00";
		String endDateTime = getFirstDayOfNextMonth(dataMonth) + " 00:00";

		LocalDateTime queryStart = processQueryDate(startDateTime);
		LocalDateTime queryEnd = processQueryDate(endDateTime);

		return ViewUtil.getFile(() -> service.prepareFile(email, qseId, txgId, resId, queryStart, queryEnd, true),
				MediaType.APPLICATION_OCTET_STREAM);
	}

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private LocalDateTime processQueryDate(String dateStr) {
        return LocalDateTime.parse(dateStr, INPUT_FORMATTER);
    }
    // 取得查詢月份的下個月之第一日
    public static String getFirstDayOfNextMonth(String yearMonth) {
		int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
		int month = Integer.parseInt(yearMonth.split("-")[1]); //月
		Calendar cal = Calendar.getInstance();
		// 設置年份
		cal.set(Calendar.YEAR, year);
		// 設置月份
		// cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.MONTH, month); //設置當前月的上一個月
		// 獲取某月最大天數
		//int lastDay = cal.getActualMaximum(Calendar.DATE);
		int lastDay = cal.getMinimum(Calendar.DATE); //獲取月份中的最小值，即第一天
		// 設置日曆中月份的最大天數
		//cal.set(Calendar.DAY_OF_MONTH, lastDay);
		cal.set(Calendar.DAY_OF_MONTH, lastDay); //下月的第一天
		// 格式化日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(cal.getTime());
	}
   
    
}
