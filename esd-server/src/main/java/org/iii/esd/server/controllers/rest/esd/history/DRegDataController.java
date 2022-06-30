package org.iii.esd.server.controllers.rest.esd.history;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.cellprocessor.ift.CellProcessor;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.integrate.DRegData;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.services.IntegrateElectricDataService;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.utils.CsvUtils;
import org.iii.esd.utils.TypedPair;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_DREG_ELECTRICDATA_BID;
import static org.iii.esd.api.RestConstants.REST_DREG_ELECTRICDATA_DETAIL;
import static org.iii.esd.api.RestConstants.REST_DREG_ELECTRICDATA_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_DREG_ELECTRICDATA_LIST;
import static org.iii.esd.api.RestConstants.REST_DREG_FIELD_BID;
import static org.iii.esd.api.RestConstants.REST_DREG_FIELD_ELECTRICDATA_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_DREG_FIELD_ELECTRICDATA_LIST;
import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;
import static org.iii.esd.utils.DatetimeUtils.DateTimeEdge.EPOCH;
import static org.iii.esd.utils.DatetimeUtils.DateTimeEdge.ETERNAL;
import static org.iii.esd.utils.DatetimeUtils.parseDateTimeOrDefault;
import static org.iii.esd.utils.TypedPair.cons;

@RestController
@Log4j2
public class DRegDataController {

    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private UserService userService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private IntegrateElectricDataService electricDataService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private IntegrateBidService bidService;
    @Autowired
    private AuthorizationHelper authorHelper;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping(REST_DREG_ELECTRICDATA_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse dregTxgList(
            @ApiParam(required = true,
                    value = "請傳入 txgId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10")
            @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10")
            @RequestParam(value = "end",
                    required = false) String _end,
            Authentication authentication) {

        return ViewUtil.getAll(() -> {
            authorHelper.checkTxgAuthorization(authentication, txgId);

            TxgProfile txg = txgService.getByTxgId(txgId);

            if (StringUtils.isEmpty(_start) && StringUtils.isEmpty(_end)) {
                log.info("get realtime dreg data");

                return electricDataService.getCurrentDRegDataByTxg(txg);
            } else {
                log.info("get history dreg data from {} to {}", _start, _end);

                LocalDateTime startTime = parseDateTimeOrDefault(_start, DATETIME_FORMATTER, EPOCH);
                LocalDateTime endTime = parseDateTimeOrDefault(_end, DATETIME_FORMATTER, ETERNAL);
                TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

                return electricDataService.getDRegDataByTxgAndDateRange(txg, dateRange);
            }
        });
    }

    @PostMapping(REST_DREG_ELECTRICDATA_DOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public void dregTxgDownload(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入 txgId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10")
            @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10")
            @RequestParam(value = "end",
                    required = false) String _end,
            Authentication authentication) {

        try {
            authorHelper.checkTxgAuthorization(authentication, txgId);

            TxgProfile txg = txgService.getByTxgId(txgId);

            if (StringUtils.isEmpty(_start) || StringUtils.isEmpty(_end)) {
                throw new WebException(Error.parameterIsRequired, "start & end");
            } else {
                log.info("download history dreg data from {} to {}", _start, _end);

                LocalDateTime startTime = parseDateTimeOrDefault(_start, DATETIME_FORMATTER, EPOCH);
                LocalDateTime endTime = parseDateTimeOrDefault(_end, DATETIME_FORMATTER, ETERNAL);
                TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

                List<DRegData> dRegData = electricDataService.getDRegDataByTxgAndDateRange(txg, dateRange);

                String csvFileName = txgId + "_" + startTime.format(DATE_FORMATTER) + CSV_EXTENSION;
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", csvFileName));

                CsvUtils.downloadCsv(response.getOutputStream(), dRegData,
                        new String[]{"time", "activePower", "voltageA","voltageB","voltageC","currentA","currentB","currentC","frequency","powerFactor","kVar", "soc", "baseFrequency"},
                        new String[]{"timeTxt", "kW", "voltageA","voltageB","voltageC","currentA","currentB","currentC","frequency","powerFactor","kVar", "soc", "baseFreq"},
                        new CellProcessor[]{
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional()});
            }
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            showError(response, ex.getResponse());
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            showError(response, new ErrorResponse(Error.internalServerError, ex.getMessage()));
        }
    }

    private void showError(HttpServletResponse response, ErrorResponse errResp) {
        try {
            OutputStream out = response.getOutputStream();
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            out.write(new Gson().toJson(errResp).getBytes());
            out.flush();
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @GetMapping(REST_DREG_ELECTRICDATA_DETAIL)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse dregDetail(
            @ApiParam(required = true,
                    value = "請傳入 txgId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10")
            @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10")
            @RequestParam(value = "end",
                    required = false) String _end,
            Authentication authentication) {
        return ViewUtil.getAll(() -> {
            authorHelper.checkTxgAuthorization(authentication, txgId);

            TxgProfile txg = txgService.getByTxgId(txgId);

            if (StringUtils.isEmpty(_start) && StringUtils.isEmpty(_end)) {
                return electricDataService.getCurrentDRegDetailByTxg(txg);
            } else {
                LocalDateTime startTime = parseDateTimeOrDefault(_start, DATETIME_FORMATTER, EPOCH);
                LocalDateTime endTime = parseDateTimeOrDefault(_end, DATETIME_FORMATTER, ETERNAL);
                TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

                return electricDataService.getDRegDetailByTxgAndDateRange(txg, dateRange);
            }
        });
    }

    @GetMapping(REST_DREG_ELECTRICDATA_BID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse dregBidInfo(
            @ApiParam(required = true,
                    value = "請傳入 txgId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "2020-12-01")
            @RequestParam(value = "date",
                    required = false) String date,
            Authentication authentication) {
        return ViewUtil.getAll(() -> {
            authorHelper.checkTxgAuthorization(authentication, txgId);

            TxgProfile txg = txgService.getByTxgId(txgId);
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);

            return electricDataService.getDRegBidInfoByTxgAndDate(txg, localDate);
        });
    }


    @GetMapping(REST_DREG_FIELD_BID)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse dregFieldBidInfo(
            @ApiParam(required = true,
                    value = "請傳入 resId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String resId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "2020-12-01")
            @RequestParam(value = "date",
                    required = false) String date,
            Authentication authentication) {
        return ViewUtil.getAll(() -> {
            authorHelper.checkResAuthorization(authentication, resId);

            TxgFieldProfile res = resService.getByResId(resId);
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);

            return electricDataService.getDRegBidInfoByResAndDate(res, localDate);
        });
    }


    @GetMapping(REST_DREG_FIELD_ELECTRICDATA_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public ApiResponse dregResList(
            @ApiParam(required = true,
                    value = "請傳入 resId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String resId,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10")
            @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10")
            @RequestParam(value = "end",
                    required = false) String _end,
            Authentication authentication) {
        return ViewUtil.getAll(() -> {
            authorHelper.checkResAuthorization(authentication, resId);

            TxgFieldProfile res = resService.getByResId(resId);

            if (StringUtils.isEmpty(_start) && StringUtils.isEmpty(_end)) {
                return electricDataService.getCurrentDRegDataByRes(res);
            } else {
                LocalDateTime startTime = parseDateTimeOrDefault(_start, DATETIME_FORMATTER, EPOCH);
                LocalDateTime endTime = parseDateTimeOrDefault(_end, DATETIME_FORMATTER, ETERNAL);
                TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

                return electricDataService.getDRegDataByResAndDateRange(res, dateRange);
            }
        });
    }

    @PostMapping(REST_DREG_FIELD_ELECTRICDATA_DOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    public void dregResDownload(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入 resId",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String resId,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10")
            @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10")
            @RequestParam(value = "end",
                    required = false) String _end,
            Authentication authentication) {
        try {
            authorHelper.checkResAuthorization(authentication, resId);

            TxgFieldProfile res = resService.getByResId(resId);

            if (StringUtils.isEmpty(_start) && StringUtils.isEmpty(_end)) {
                throw new WebException(Error.parameterIsRequired, "start & end");
            } else {
                LocalDateTime startTime = parseDateTimeOrDefault(_start, DATETIME_FORMATTER, EPOCH);
                LocalDateTime endTime = parseDateTimeOrDefault(_end, DATETIME_FORMATTER, ETERNAL);
                TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

                List<DRegData> dRegData = electricDataService.getDRegDataByResAndDateRange(res, dateRange);
                String csvFileName = resId + "_" + startTime.format(DATE_FORMATTER) + CSV_EXTENSION;
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", csvFileName));

                CsvUtils.downloadCsv(response.getOutputStream(), dRegData,
                        new String[]{"time", "activePower", "voltageA","voltageB","voltageC","currentA","currentB","currentC","frequency","powerFactor","kVar", "soc", "baseFrequency"},
                        new String[]{"timeTxt", "kW", "voltageA","voltageB","voltageC","currentA","currentB","currentC","frequency","powerFactor","kVar", "soc", "baseFreq"},
                        new CellProcessor[]{
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional(),
                                new org.supercsv.cellprocessor.Optional()});
            }
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            showError(response, ex.getResponse());
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            showError(response, new ErrorResponse(Error.internalServerError, ex.getMessage()));
        }
    }
}