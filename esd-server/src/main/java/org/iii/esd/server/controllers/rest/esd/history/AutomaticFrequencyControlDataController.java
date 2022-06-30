package org.iii.esd.server.controllers.rest.esd.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import org.iii.esd.Constants;
import org.iii.esd.afc.performance.Result;
import org.iii.esd.afc.service.AfcPerformanceService;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.service.AutomaticFrequencyControlMeasureService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.mongo.vo.AfcLog;

import static org.iii.esd.Constants.CONTENT_TYPE_CSV;
import static org.iii.esd.Constants.CONTENT_TYPE_JSON;
import static org.iii.esd.Constants.ROLE_AFCADMIN;
import static org.iii.esd.Constants.ROLE_AFCUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_DATA_LIST;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_PERFORMANCE_DATA_LIST;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_DOWNLOAD_BYDATE;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_RECALCULATE;
import static org.iii.esd.api.RestConstants.REST_AUTOMATICFREQUENCYCONTROL_TC_AUTOSYNC;
import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;

@RestController
@Log4j2
// @Api(tags = "AutomaticFrequencyControlData",
//         description = "調頻備轉歷史資料查詢")
public class AutomaticFrequencyControlDataController {

    @Autowired
    private AutomaticFrequencyControlService afcService;

    @Autowired
    private AfcPerformanceService afcPerformanceService;


    @Autowired
    private AutomaticFrequencyControlMeasureService afcMeasure;

    @Value("${temp_folder}")
    private String tempFolder;

    // @GetMapping(REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_DOWNLOAD_BYDATE)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER})
    // @ApiOperation(value = "afcDownloadPerformanceByDate",
    //         notes = "調頻備轉績效資料下載")
    public ResponseEntity downloadPerformance(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @RequestParam(value = "id",
                    required = true) Long _id,
            @ApiParam(required = true,
                    value = "請傳入開始日期",
                    example = "2021-06-20") @RequestParam(value = "start",
                    required = true) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束日期",
                    example = "2021-06-20") @RequestParam(value = "end",
                    required = true) String _end
    ) throws IOException {

        ErrorResponse errorResponse = checkParameter(_id, String.format("%s 00:00:00", _start), String.format("%s 23:59:59", _end));
        if (errorResponse != null) {
            response.setContentType(CONTENT_TYPE_JSON);
            OutputStream out = response.getOutputStream();
            out.write(new Gson().toJson(errorResponse).getBytes());
            out.flush();
            out.close();
            return null;
        }

        Date dtStart;
        Date dtEnd;

        List<AutomaticFrequencyControlMeasure> dataList;
        try {
            dtStart = Constants.TIMESTAMP_FORMAT.parse(_start + " 00:00:00");
            dtEnd = Constants.TIMESTAMP_FORMAT.parse(_end + " 23:59:59");
            dataList = afcMeasure.findAllByAfcIdAndTimeRangeAndType(_id, dtStart, dtEnd, "spm");
        } catch (Exception ex) {
            //
            return null;
        }
        if (dataList == null || dataList.size() == 0) {return null;}

        String sFile = String.format("AFC_SPM_%s-%s.csv", _start.replace("-", ""), _end.replace("-", ""));

        PrintWriter out = response.getWriter();
        response.setHeader("Content-Disposition", "attachment;filename=\"" + sFile + "\"");
        response.setContentType("text/csv; charset=UTF-8");

        HashMap<String, Object> Recs = new HashMap<String, Object>();
        SimpleDateFormat dfKey = new SimpleDateFormat("yyyyMMdd_HHmm");
        dtStart = dataList.get(0).getTimestamp();
        dtEnd = dataList.get(0).getTimestamp();
        for (AutomaticFrequencyControlMeasure data : dataList) {
            if (dtStart.compareTo(data.getTimestamp()) > 0) {dtStart = data.getTimestamp();}
            if (dtEnd.compareTo(data.getTimestamp()) < 0) {dtEnd = data.getTimestamp();}

            String sKey = dfKey.format(data.getTimestamp());
            Recs.put(sKey, data.getValue().doubleValue());
        }
        System.out.println(dtStart);
        System.out.println(dtEnd);

        String sOut = "AFC SPM";
        for (int i = 0; i < 1440; i += 15) {
            sOut += String.format(",%02d:%02d", i / 60, i % 60);
        }
        out.println(sOut);

        try {
            SimpleDateFormat dfOut = new SimpleDateFormat("yyyy/MM/dd");
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(dtStart);
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(dtEnd);

            String sKey;
            for (Date date = dtStart; calFrom.before(calTo); calFrom.add(Calendar.DATE, 1), date = calFrom.getTime()) {
                sOut = dfOut.format(date);
                for (int i = 0; i < 1440; i += 15) {
                    try {
                        sKey = String.format("%s_%02d%02d", dfOut.format(date).replace("/", ""), i / 60, i % 60);
                        sOut += "," + Recs.get(sKey).toString();
                    } catch (Exception ex) {
                        sOut += ",";
                    }
                }
                out.println(sOut);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    // @GetMapping(REST_AUTOMATICFREQUENCYCONTROL_DOWNLOAD)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER})
    // @ApiOperation(value = "afcDownload",
    //         notes = "AFC資料下載")
    public ResponseEntity downloadAFC(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @RequestParam(value = "id",
                    required = true) Long _id,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2021-01-21 11:57:00") @RequestParam(value = "start",
                    required = true) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2021-01-21 11:58:00") @RequestParam(value = "end",
                    required = true) String _end,
            @ApiParam(required = true,
                    value = "是否包含sbspm",
                    example = "1") @RequestParam(value = "withSbspm",
                    required = true) Integer _withSbspm,
            @ApiParam(required = true,
                    value = "單檔最大筆數(0:不切檔)",
                    example = "0") @RequestParam(value = "maxRows",
                    required = true) Integer _maxRows,
            @ApiParam(required = true,
                    value = "QSE_ID",
                    example = "1") @RequestParam(value = "QSE_ID",
                    required = true) String _QSE_ID,
            @ApiParam(required = true,
                    value = "GROUP_ID",
                    example = "1") @RequestParam(value = "GROUP_ID",
                    required = true) String _GROUP_ID,
            @ApiParam(required = true,
                    value = "RESOURCE_ID",
                    example = "1") @RequestParam(value = "RESOURCE_ID",
                    required = true) String _RESOURCE_ID,
            @ApiParam(required = true,
                    value = "SERVICE_ITEM",
                    example = "AFC") @RequestParam(value = "SERVICE_ITEM",
                    required = true) String _SERVICE_ITEM
    ) throws IOException {

        ErrorResponse errorResponse = checkParameter(_id, _start, _end);
        if (errorResponse != null) {
            response.setContentType(CONTENT_TYPE_JSON);
            OutputStream out = response.getOutputStream();
            out.write(new Gson().toJson(errorResponse).getBytes());
            out.flush();
            out.close();
            return null;
        }

        Date dtStart;
        Date dtEnd;
        List<AutomaticFrequencyControlLog> dataList;
        try {
            dtStart = Constants.TIMESTAMP_FORMAT.parse(_start);
            dtEnd = Constants.TIMESTAMP_FORMAT.parse(_end);
            dataList = afcService.findAutomaticFrequencyControlLogByIdAndTime(_id, dtStart, dtEnd);
        } catch (Exception ex) {
            //
            return null;
        }
        if (dataList == null || dataList.size() == 0) {return null;}

        int nRecordCount = dataList.size();
        int nRecordIndex = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
        SimpleDateFormat dfUtc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        String sOutput = String.format("AFC_%s-%s_%d", df.format(dtStart), df.format(dtEnd), nRecordCount);

        HashMap<String, Object> row = new HashMap<String, Object>();
        row.put("QSE_ID", _QSE_ID);
        row.put("GROUP_ID", _GROUP_ID);
        row.put("RESOURCE_ID", _RESOURCE_ID);
        row.put("SERVICE_ITEM", _SERVICE_ITEM);
        row.put("SUPWH", "");
        row.put("DMDWH", "");

        String sHeader =
                "QSE_ID,GROUP_ID,RESOURCE_ID,SERVICE_ITEM,DATA_TIMESTAMP,HZ,V_A,V_B,V_C,A_A,A_B,A_C,TOT_W,SUPWH,DMDWH,TOT_VAR,TOT_PF,SOC";
        if (_withSbspm == 1) {
            sHeader += ",SBSPM,Status";
        }
        String[] csvHeader = sHeader.split(",");

        CsvMapWriter csvWriter = null;
        String sOutputFolder = tempFolder;
        if (_maxRows == 0) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + sOutput + ".csv");
            csvWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
            csvWriter.writeHeader(csvHeader);
        } else {
            UUID uuid = UUID.randomUUID();
            sOutputFolder = tempFolder + "/" + uuid.toString();
            Path path = Paths.get(sOutputFolder);
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                System.out.println("Folder created: " + sOutputFolder);
            }
        }

        for (AutomaticFrequencyControlLog data : dataList) {
            if (csvWriter == null) {
                FileWriter writer = new FileWriter(sOutputFolder +
                        String.format("/%s_%d-%d.csv", sOutput, nRecordIndex + 1, Math.min(nRecordCount, nRecordIndex + _maxRows)));
                csvWriter = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);
                csvWriter.writeHeader(csvHeader);
            }
            row.put("DATA_TIMESTAMP", dfUtc.format(data.getTimestamp()));
            row.put("HZ", Math.round(data.getActualFrequency().doubleValue() * 100));
            row.put("V_A", Math.round(data.getVoltageA().doubleValue() / 10));
            row.put("V_B", Math.round(data.getVoltageB().doubleValue() / 10));
            row.put("V_C", Math.round(data.getVoltageC().doubleValue() / 10));
            row.put("A_A", Math.round(data.getCurrentA().doubleValue() * 100));
            row.put("A_B", Math.round(data.getCurrentB().doubleValue() * 100));
            row.put("A_C", Math.round(data.getCurrentC().doubleValue() * 100));
            row.put("TOT_W", Math.round(data.getActivePower().doubleValue() * 100));
            row.put("TOT_VAR", Math.round(data.getKVAR().doubleValue() * 100));
            row.put("TOT_PF", Math.round(data.getPowerFactor().doubleValue() * 100));
            row.put("SOC", Math.round(data.getSoc().doubleValue() * 100));
            row.put("SBSPM", data.getSbspm());
            row.put("Status", data.getStatus());

            csvWriter.write(row, csvHeader);
            nRecordIndex++;
            if (_maxRows > 0 && (nRecordIndex % _maxRows) == 0) {
                csvWriter.flush();
                csvWriter.close();

                csvWriter = null;
            }
        }
        csvWriter.flush();
        csvWriter.close();

        if (_maxRows == 0) {
            return null; //ResponseEntity.ok();
        }

        ZipFolder(sOutputFolder, sOutputFolder + ".zip");
        Path target = Paths.get(sOutputFolder); //Path.of(sOutputFolder);
        for (File file : target.toFile().listFiles()) {
            file.delete();
        }
        target.toFile().delete();

        target = Paths.get(sOutputFolder + ".zip"); //Path.of(sOutputFolder + ".zip"
        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType("application/zip"))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sOutput + ".zip" + "\"")
                             .body(new UrlResource(target.toUri()));
    }

    private boolean ZipFolder(String folder, String target) {
        try {
            FileOutputStream fos = new FileOutputStream(target);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(folder);

            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                FileInputStream fis = new FileInputStream(folder + "/" + childFile.getName());
                ZipEntry zipEntry = new ZipEntry(childFile.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            zipOut.close();
            fos.close();

            return true;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return false;
    }

    // @GetMapping(REST_AUTOMATICFREQUENCYCONTROL_DATA_LIST)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_AFCADMIN, ROLE_AFCUSER})
    // @ApiOperation(value = "afcDataList",
    //         notes = "調頻備轉用電歷史資料查詢")
    public ApiResponse afcDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @RequestParam(value = "id",
                    required = false) Long id,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10") @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10") @RequestParam(value = "end",
                    required = false) String _end
    ) {

        ErrorResponse errorResponse = checkParameter(id, _start, _end);
        if (errorResponse != null) {
            return errorResponse;
        } else {
            try {
                Date start = Constants.TIMESTAMP_FORMAT.parse(_start);
                Date end = Constants.TIMESTAMP_FORMAT.parse(_end);
                if (checkTimeInterval(start, end)) {
                    return new ErrorResponse(Error.timeIntervalInvalid);
                }
                return new ListResponse<AutomaticFrequencyControlLog>(
                        afcService.findAutomaticFrequencyControlLogByIdAndTime(id, start, end));
            } catch (ParseException e) {
                return new ErrorResponse(Error.dateFormatInvalid);
            } catch (Exception e) {
                log.error(e.getMessage());
                return new ErrorResponse(Error.internalServerError, e.getMessage());
            }
        }
    }

    // @GetMapping(REST_AUTOMATICFREQUENCYCONTROL_PERFORMANCE_DATA_LIST)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_AFCADMIN, ROLE_AFCUSER})
    // @ApiOperation(value = "afcPerformanceDataList",
    //         notes = "調頻備轉績效資料查詢")
    public ApiResponse afcPerformanceDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @RequestParam(value = "id",
                    required = false) Long id,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10") @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10") @RequestParam(value = "end",
                    required = false) String _end
    ) {

        ErrorResponse errorResponse = checkParameter(id, _start, _end);
        if (errorResponse != null) {
            return errorResponse;
        } else {
            try {
                Date start = Constants.TIMESTAMP_FORMAT.parse(_start);
                Date end = Constants.TIMESTAMP_FORMAT.parse(_end);
                if (checkTimeInterval(start, end)) {
                    return new ErrorResponse(Error.timeIntervalInvalid);
                }
                return new ListResponse<AfcLog>(afcService.getAfcLogList(id, start.getTime(), end.getTime()));
            } catch (ParseException e) {
                return new ErrorResponse(Error.dateFormatInvalid);
            } catch (Exception e) {
                log.error(e.getMessage());
                return new ErrorResponse(Error.internalServerError, e.getMessage());
            }
        }
    }

    // @PostMapping(REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_RECALCULATE)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN})
    // @ApiOperation(value = "recalculate",
    //         notes = "調頻備轉績效資料重算")
    public ApiResponse recalculate(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") Long id,
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
            @ApiParam(required = false,
                    value = "請傳入重算類型",
                    example = "spm, sbspm, all")
            @RequestParam(value = "type",
                    required = false) String type) {
        ErrorResponse errorResponse = checkParameter(id, _start, _end);
        if (errorResponse != null) {
            return errorResponse;
        } else {
            try {
                Date start = Constants.TIMESTAMP_FORMAT.parse(_start);
                Date end = Constants.TIMESTAMP_FORMAT.parse(_end);
                String sType = StringUtils.trimToEmpty(type).toLowerCase(Locale.ROOT);

                if (StringUtils.isEmpty(type) || "all".equals(sType)) {
                    Result sbspmResult = afcPerformanceService.calculateSbspm(id, start, end);
                    log.info("sbspm update {} rowcount", sbspmResult.getCount());
                    Result spmResult = afcPerformanceService.calculateSpm(id, start, end);
                    log.info("spm update {} rowcount", spmResult.getCount());
                } else if ("spm".equals(sType)) {
                    Result spmResult = afcPerformanceService.calculateSpm(id, start, end);
                    log.info("spm update {} rowcount", spmResult.getCount());
                } else if ("sbspm".equals(sType)) {
                    Result sbspmResult = afcPerformanceService.calculateSbspm(id, start, end);
                    log.info("sbspm update {} rowcount", sbspmResult.getCount());
                } else {
                    return new ErrorResponse(Error.badRequest, type);
                }

                return new SuccessfulResponse();
            } catch (ParseException e) {
                return new ErrorResponse(Error.dateFormatInvalid);
            } catch (Exception e) {
                log.error(e.getMessage());
                return new ErrorResponse(Error.internalServerError, e.getMessage());
            }
        }
    }

    // @PostMapping(REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_DOWNLOAD)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER})
    // @ApiOperation(value = "afcPerformanceDataList",
    //         notes = "調頻備轉績效資料下載")
    public void downloadCsv(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @PathVariable("id") Long id,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-12-01 17:10:10") @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-12-01 17:25:10") @RequestParam(value = "end",
                    required = false) String _end) throws IOException {
        OutputStream out = null;
        Writer writer = null;
        ICsvBeanWriter csvWriter = null;
        response.setContentType(CONTENT_TYPE_JSON);

        try {
            out = response.getOutputStream();

            ErrorResponse errorResponse = checkParameter(id, _start, _end);
            if (errorResponse != null) {
                out.write(new Gson().toJson(errorResponse).getBytes());
                out.flush();
                return;
            } else {
                Date start = Constants.TIMESTAMP_FORMAT.parse(_start);
                Date end = Constants.TIMESTAMP_FORMAT.parse(_end);

                writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                writer.write('\uFEFF'); // BOM for UTF-*
                csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);

                List<AfcLog> dataList = afcService.getAfcLogList(id, start.getTime(), end.getTime());
                if (dataList.size() > 0) {
                    String csvFileName = "afc_log" + CSV_EXTENSION;
                    response.setContentType(CONTENT_TYPE_CSV);
                    String headerKey = "Content-Disposition";
                    String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
                    response.setHeader(headerKey, headerValue);

                    String[] header =
                            {"afcId", "timestamp", "voltageA", "voltageB", "voltageC", "frequency", "essPower", "essPowerRatio", "kvar",
                                    "powerFactor", "soc", "sbspm", "spm"};
                    csvWriter.writeHeader(header);

                    String[] fieldMapping =
                            {"AfcId", "Timestamp", "VoltageA", "VoltageB", "VoltageC", "Frequency", "EssPower", "EssPowerRatio", "Kvar",
                                    "PowerFactor", "Soc", "Sbspm", "Spm"};
                    for (AfcLog data : dataList) {
                        csvWriter.write(data, fieldMapping);
                    }

                    csvWriter.flush();
                } else {
                    out.write(new Gson().toJson(new ErrorResponse(Error.operationFailed, "date interval has no data")).getBytes());
                    out.flush();
                }
            }
        } catch (ParseException e) {
            log.error(e);
            out.write(new Gson().toJson(new ErrorResponse(Error.dateFormatInvalid)).getBytes());
            out.flush();
        } catch (Exception e) {
            log.error(e);
            out.write(new Gson().toJson(new ErrorResponse(Error.internalServerError, e.getMessage())).getBytes());
            out.flush();
        } finally {
            if (csvWriter != null) {
                csvWriter.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    // @GetMapping(REST_AUTOMATICFREQUENCYCONTROL_TC_AUTOSYNC)
    // @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_SIUSER})
    // @ApiOperation(value = "afcTC_sync_data",
    //         notes = "AFC_TC補傳資料")
    public ApiResponse afcTC_sync_data(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @RequestParam(value = "id",
                    required = true) Long _id,
            @ApiParam(required = true,
                    value = "請傳入開始日期",
                    example = "2021-06-20") @RequestParam(value = "start",
                    required = true) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束日期",
                    example = "2021-06-20") @RequestParam(value = "end",
                    required = true) String _end
    ) throws IOException {

        ErrorResponse errorResponse = checkParameter(_start, _end);
        if (errorResponse != null) {return errorResponse;}

        try {
            Optional<AutomaticFrequencyControlProfile> oAfcProfile = afcService.findAutomaticFrequencyControlProfile(_id);
            if (oAfcProfile == null) {
                return new ErrorResponse(Error.invalidParameter, _id); // "afcID not found"
            }

            AutomaticFrequencyControlProfile obj = oAfcProfile.get();
            String posturl = obj.getPosturl();
            //posturl = "http://localhost:58001/esd/history/afc/data_list?id={$id}&start={$start}&end={$end}";
            posturl = posturl.replaceAll("\\{\\$id\\}", _id.toString());
            posturl = posturl.replaceAll("\\{\\$start\\}", _start);
            posturl = posturl.replaceAll("\\{\\$end\\}", _end);
            System.out.println(posturl);

            // preparing http get.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> reqEntity = new HttpEntity<String>(null, headers);

            RestTemplate restTemplate = new RestTemplate();
            try {
                ResponseEntity<String> postResponse = restTemplate.exchange(posturl, HttpMethod.GET, reqEntity, String.class);
                //HttpStatus statusCode = postResponse.getStatusCode();
                //System.out.println(statusCode); //respEntity.getStatusCodeValue());
                //System.out.println("*********"); //respEntity.getStatusCodeValue());

                // only 200 will run to here.
                System.out.println(postResponse.getBody()); //respEntity.getStatusCodeValue());
                return new SuccessfulResponse();
            } catch (Exception ex) {
                log.error(ex.getMessage());
                return new ErrorResponse(Error.forbidden, ex.getMessage());
            }
        } catch (Exception ex) {
            log.error(ex);
            return new ErrorResponse(Error.forbidden, ex.getMessage());
        } finally {
            //
        }
    }

    private boolean checkTimeInterval(Date start, Date end) {
        int minute = 20;
        return end.getTime() - start.getTime() > minute * 60 * 1000;
    }

    private ErrorResponse checkParameter(String start, String end) {
        if (start == null) {
            return new ErrorResponse(Error.parameterIsRequired, "start");
        }

        if (end == null) {
            return new ErrorResponse(Error.parameterIsRequired, "end");
        }

        try {
            if (Constants.TIMESTAMP_FORMAT.parse(end).before(Constants.TIMESTAMP_FORMAT.parse(start))) {
                return new ErrorResponse(Error.invalidParameter, "Invalid start, end or end is before start");
            }
        } catch (ParseException e) {
            return new ErrorResponse(Error.dateFormatInvalid);
        }
        return null;
    }

    private ErrorResponse checkParameter(Long id, String start, String end) {
        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        if (start == null) {
            return new ErrorResponse(Error.parameterIsRequired, "start");
        }

        if (end == null) {
            return new ErrorResponse(Error.parameterIsRequired, "end");
        }

        try {
            if (Constants.TIMESTAMP_FORMAT.parse(end).before(Constants.TIMESTAMP_FORMAT.parse(start))) {
                return new ErrorResponse(Error.invalidParameter, "Invalid start, end or end is before start");
            }

            if (isInvalidAfcId(id)) {
                return new ErrorResponse(Error.invalidParameter, id);
            }
        } catch (ParseException e) {
            return new ErrorResponse(Error.dateFormatInvalid);
        }
        return null;
    }

    private boolean isInvalidAfcId(Long id) {
        Optional<AutomaticFrequencyControlProfile> oAfcProfile = afcService.findAutomaticFrequencyControlProfile(id);
        if (!oAfcProfile.isPresent()) {
            return true;
        } else {
            AutomaticFrequencyControlProfile afcProfile = oAfcProfile.get();
            if (EnableStatus.disable.equals(afcProfile.getEnableStatus())) {
                log.warn("AutomaticFrequencyControl:{} is NotEnabled", afcProfile.getId());
                return true;
            } else {
                return false;
            }
        }
    }

}