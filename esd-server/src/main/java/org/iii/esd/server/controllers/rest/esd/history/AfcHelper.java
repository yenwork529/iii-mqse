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
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import org.iii.esd.Constants;
import org.iii.esd.afc.service.AfcPerformanceService;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
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
import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;

@Service
@Log4j2
public class AfcHelper {

    @Autowired
    private AutomaticFrequencyControlService afcService;

    @Autowired
    private AfcPerformanceService afcPerformanceService;


    @Autowired
    private AutomaticFrequencyControlMeasureService afcMeasure;

    @Value("${temp_folder}")
    private String tempFolder;

    public ResponseEntity downloadPerformance(HttpServletResponse response, Long _id, String _start, String _end) throws IOException {
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

    public ResponseEntity downloadAFC(HttpServletResponse response, Long _id, String _start, String _end, Integer _withSbspm,
            Integer _maxRows, String _QSE_ID, String _GROUP_ID, String _RESOURCE_ID, String _SERVICE_ITEM) throws IOException {
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

        zipFolder(sOutputFolder, sOutputFolder + ".zip");
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

    public boolean zipFolder(String folder, String target) {
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

    public void downloadCsv(HttpServletResponse response, Long id, String _start, String _end) throws IOException {
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

    public ApiResponse afcTcSyncData(Long _id, String _start, String _end
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
