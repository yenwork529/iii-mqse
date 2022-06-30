package org.iii.esd.server.controllers.rest.esd.history;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.ift.CellProcessor;

import org.iii.esd.Constants;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.DataResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.vo.MainBoard;
import org.iii.esd.api.vo.ResElectricData;
import org.iii.esd.api.vo.SpinReserveHistoryData;
import org.iii.esd.api.vo.SpinReserveHistoryDetailData;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
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
import org.iii.esd.server.services.MainBoardService;
import org.iii.esd.utils.CsvUtils;
import org.iii.esd.utils.DatetimeUtils;
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
import static org.iii.esd.api.RestConstants.REST_FIELD_ELECTRICDATA_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_FIELD_ELECTRICDATA_LIST;
import static org.iii.esd.api.RestConstants.REST_GROUP_ELECTRICDATA_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_SPINRESERVE_DETAIL_LIST;
import static org.iii.esd.api.RestConstants.REST_SPINRESERVE_ELECTRICDATA_LIST;
import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;

@RestController
@Log4j2
@Api(tags = "ElectricData",
        description = "用電歷史資料查詢")
public class ElectricDataController {

    @Autowired
    private IntegrateElectricDataService electricDataService;

    @Autowired
    private IntegrateRelationService relationService;

    @Autowired
    private IntegrateDataService dataService;

    @Autowired
    private IntegrateBidService bidService;

    @Autowired
    private TxgService txgService;

    @Autowired
    private TxgFieldService resService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationHelper authorHelper;

    @Autowired
    private MainBoardService mainBoardService;

    @GetMapping(REST_SPINRESERVE_DETAIL_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "srDetailList",
            notes = "即時備轉用電歷史明細查詢")
    public ApiResponse srDetailList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
            @ApiParam(required = false,
                    value = "請傳入日期",
                    example = "2020-10-10")
            @RequestParam(value = "date",
                    required = false) String date,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "T1")
            @RequestParam(value = "dataType",
                    required = false) DataType dataType,
            Authentication authentication) {

        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        try {
            authorHelper.checkTxgAuthorization(authentication, txgId);
            List<TxgFieldProfile> fields = resService.findByTxgId(txgId);
            TypedPair<Date> range = getDateRange(date);

            // 建構場域基本資料
            // key: field.id & field.name
            // value: 各場域的 ElectricData
            Map<TypedPair<String>, List<ResElectricData>> fieldData =
                    electricDataService.buildFieldData(fields, range, dataType);

            if (MapUtils.isEmpty(fieldData)) {
                // 若沒有 ElectricData 就回傳空的
                return new DataResponse<>(electricDataService.buildEmptyDetailData(fields, range));
            }

            SpinReserveHistoryDetailData detailData = electricDataService.buildSrDetailFromFieldData(fieldData);

            return new DataResponse<>(detailData);
        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(e.getError());
        }
    }

    private TypedPair<Date> getDateRange(String date) throws WebException {
        try {
            // 設定起迄日，以天為單位
            Date start = date != null ? Constants.DATE_FORMAT.parse(date) :
                    DatetimeUtils.getFirstHourOfDay(new Date());
            Date end = DatetimeUtils.add(start, Calendar.DATE, 1);

            return TypedPair.cons(start, end);
        } catch (ParseException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new WebException(Error.invalidParameter, date);
        }
    }

    @GetMapping(REST_SPINRESERVE_ELECTRICDATA_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "srDataList",
            notes = "即時備轉用電歷史資料查詢")
    public ApiResponse srDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
            @ApiParam(required = false,
                    value = "請傳入日期",
                    example = "2020-10-10")
            @RequestParam(value = "date",
                    required = false) String date,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "T1")
            @RequestParam(value = "dataType",
                    required = false) DataType dataType,
            Authentication authentication) {

        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        try {
            authorHelper.checkTxgAuthorization(authentication, txgId);

            // 設定起迄日，以天為單位
            Date start = date != null ? Constants.DATE_FORMAT.parse(date) :
                    DatetimeUtils.getFirstHourOfDay(new Date());
            Date end = DatetimeUtils.add(start, Calendar.DATE, 1);

            // 取得 SR 資料
            TxgProfile txg = txgService.getByTxgId(txgId);
            if (EnableStatus.disable.equals(txg.getEnableStatus())) {
                log.warn("SpinReserve:{} is NotEnabled", txg.getId());
                return new ErrorResponse(Error.isNotEnabled, txgId);
            } else {
                MainBoard.State state = mainBoardService.getStateOfTxg(txg);
                List<SpinReserveHistoryData> historyData = electricDataService.buildTxgHistoryData(state, txgId, start, end, dataType, txg);
                Map<String, Object> historyMeta = electricDataService.buildTxgHistoryMeta(historyData, state, txg, start);

                return new ListResponse<>(historyData, historyMeta);
            }
        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(e.getError());
        } catch (ParseException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(Error.dateFormatInvalid);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
    }

    @GetMapping(REST_FIELD_ELECTRICDATA_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "fieldDataList",
            notes = "場域用電歷史資料查詢")
    public ApiResponse fieldDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String resId,
            @ApiParam(required = false,
                    value = "請傳入日期",
                    example = "2020-10-10")
            @RequestParam(value = "date",
                    required = false) String date,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "T1")
            @RequestParam(value = "dataType",
                    required = false) DataType dataType,
            Authentication authentication) {

        if (!resService.isValidResId(resId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        try {
            authorHelper.checkResAuthorization(authentication, resId);

            TxgFieldProfile res = resService.getByResId(resId);
            TxgProfile txg = txgService.getByTxgId(res.getTxgId());

            Date start = date != null ? Constants.DATE_FORMAT.parse(date) :
                    DatetimeUtils.getFirstHourOfDay(new Date());

            Date end = DatetimeUtils.add(start, Calendar.DATE, 1);

            // 場域用電
            MainBoard.State state = mainBoardService.getStateOfTxg(txg);
            List<SpinReserveHistoryData> historyData = electricDataService.buildResHistoryData(state, resId, dataType, start, end, res);
            Map<String, Object> historyMeta = electricDataService.buildResHistoryMeta(historyData, state, res, start);

            return new ListResponse<>(historyData, historyMeta);

            // List<SpinReserveHistoryData> dataList = electricDataService.buildResHistoryData(resId, dataType, start, end, resProfile);
            // return new ListResponse<>(dataList);
        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(e.getError());
        } catch (ParseException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(Error.dateFormatInvalid);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
    }

    @PostMapping(REST_GROUP_ELECTRICDATA_DOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "groupDataDownload",
            notes = "依據日期匯出交易群組用電歷史資料")
    public void groupDataDownload(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "2020-10-10")
            @RequestParam(value = "date",
                    required = false) String date,
            @ApiParam(required = true,
                    value = "請傳入天數",
                    example = "1",
                    defaultValue = "1")
            @RequestParam(value = "count",
                    required = false,
                    defaultValue = "1") Integer count,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "T1")
            @RequestParam(value = "dataType",
                    required = false) DataType dataType,
            Authentication authentication) {

        if (!txgService.isValidTxgId(txgId)) {
            showError(response, Error.parameterIsRequired, "id");
            return;
        }

        try {
            authorHelper.checkTxgAuthorization(authentication, txgId);

            if (count < 1 || count > 31) {
                showError(response, Error.invalidParameter, "count");
                return;
            }

            Date start = date != null ?
                    Constants.DATE_FORMAT.parse(date) :
                    DatetimeUtils.getFirstHourOfDay(new Date());
            Date end = DatetimeUtils.add(start, Calendar.DATE, count);

            TxgProfile txg = txgService.getByTxgId(txgId);

            List<SpinReserveHistoryData> historyData = electricDataService.buildTxgHistoryExport(txgId, start, end, dataType, txg);
            // List<ResElectricData> edlist = electricDataService.getByResProfileAndDataTypeAndTime(txg, dataType, start, end);


            String csvFileName = txgId + "_" + date + CSV_EXTENSION;
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", csvFileName));

            CsvUtils.downloadCsv(response.getOutputStream(), historyData,
                    new String[]{"time", "kW", "kWh"},
                    new String[]{"time", "acPower", "totalkWh"},
                    new CellProcessor[]{
                            new FmtDate("yyyy-MM-dd HH:mm:ss"),
                            new org.supercsv.cellprocessor.Optional(),
                            new org.supercsv.cellprocessor.Optional()});

        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            showError(response, Error.internalServerError, e.getMessage());
        }
    }


    @PostMapping(REST_FIELD_ELECTRICDATA_DOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "fieldDataDownload",
            notes = "依據日期匯出場域用電歷史資料")
    public void fieldDataDownload(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String resId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "2020-10-10")
            @RequestParam(value = "date",
                    required = false) String date,
            @ApiParam(required = true,
                    value = "請傳入天數",
                    example = "1",
                    defaultValue = "1")
            @RequestParam(value = "count",
                    required = false,
                    defaultValue = "1") Integer count,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "T1")
            @RequestParam(value = "dataType",
                    required = false) DataType dataType,
            Authentication authentication) {

        if (!resService.isValidResId(resId)) {
            showError(response, Error.parameterIsRequired, "id");
            return;
        }

        try {
            authorHelper.checkResAuthorization(authentication, resId);

            if (count < 1 || count > 31) {
                showError(response, Error.invalidParameter, "count");
                return;
            }

            Date start = date != null ?
                    Constants.DATE_FORMAT.parse(date) :
                    DatetimeUtils.getFirstHourOfDay(new Date());
            Date end = DatetimeUtils.add(start, Calendar.DATE, count);

            TxgFieldProfile res = resService.getByResId(resId);

            List<ResElectricData> edlist = electricDataService.getByResProfileAndDataTypeAndTime(res, dataType, start, end);

            String csvFileName = resId + "_" + date + CSV_EXTENSION;
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", csvFileName));

            CsvUtils.downloadCsv(response.getOutputStream(), edlist,
                    new String[]{"time", "kW", "kWh"},
                    new String[]{"time", "activePower", "totalkWh"},
                    new CellProcessor[]{
                            new FmtDate("yyyy-MM-dd HH:mm:ss"),
                            new org.supercsv.cellprocessor.Optional(),
                            new org.supercsv.cellprocessor.Optional()});

        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            showError(response, Error.internalServerError, e.getMessage());
        }
    }

    private void showError(HttpServletResponse response, Error err, Object... param) {
        try {
            OutputStream out = response.getOutputStream();
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            ApiResponse apiResponse = param.length > 0 ? new ErrorResponse(err, param[0]) : new ErrorResponse(err);
            out.write(new Gson().toJson(apiResponse).getBytes());
            out.flush();
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}