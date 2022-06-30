package org.iii.esd.client.afc.performance;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.client.afc.scheduler.ReadProcessJob;

import static org.iii.esd.Constants.CONTENT_TYPE_CSV;
import static org.iii.esd.Constants.CONTENT_TYPE_JSON;
import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.vo.AfcLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.gson.Gson;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class AfcLogController extends PMcalJob {

	@RequestMapping(value = "/helo", method = { RequestMethod.GET }, produces = { "application/json" })
    public ApiResponse debugHelo()
    {
		ReadProcessJob.Toggle();
        return new SuccessfulResponse();
    }
	
	@PostMapping(URI_AFC_LOG)  
	public void downloadCsv(HttpServletResponse response, @RequestParam Long profileId, @RequestParam Long start, @RequestParam Long end) throws IOException {
		OutputStream out = null;
		Writer writer = null;
		ICsvBeanWriter csvWriter = null;
		response.setContentType(CONTENT_TYPE_JSON);
		
		try {
			out = response.getOutputStream();
			if (!isValidAfcId(profileId)) {									 
				out.write(new Gson().toJson(new ErrorResponse(Error.invalidParameter, "Not a valid profileId:" + profileId)).getBytes());
				out.flush();
				return;
			}

			if (start==null || end==null || new Date(end).before(new Date(start))) {
				out.write(new Gson().toJson(new ErrorResponse(Error.invalidParameter, "Invalid start, end or end is before start")).getBytes());
				out.flush();
				return;				
			}

	        writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
	        writer.write('\uFEFF'); // BOM for UTF-*
	        csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);	        

	        List<AfcLog> dataList = afcService.getAfcLogList(profileId, start, end);
	        if (dataList.size()>0) {
			    String csvFileName = "afc_log" + CSV_EXTENSION;	        
			    response.setContentType(CONTENT_TYPE_CSV);
			    String headerKey = "Content-Disposition";
			    String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
			    response.setHeader(headerKey, headerValue);

			    String[] header = {"afcId", "timestamp", "frequency", "essPower", "essPowerRatio", "sbspm", "spm"};	        	        
			    csvWriter.writeHeader(header);
			        
			    String[] fieldMapping = {"AfcId", "Timestamp", "Frequency", "EssPower", "EssPowerRatio", "Sbspm", "Spm"};
			    for (AfcLog data : dataList) {
			    	csvWriter.write(data, fieldMapping);
			    }
			    csvWriter.flush();
			} else {					 
				out.write(new Gson().toJson(new ErrorResponse(Error.operationFailed, "date interval has no data")).getBytes());
				out.flush();
			}														
		} catch (Exception ex) {		
			log.error(ex);					 
			out.write(new Gson().toJson(new ErrorResponse(Error.internalServerError, ex.getMessage())).getBytes());
			out.flush();			
		} finally {
			if (csvWriter!=null) {
				csvWriter.close();
			}
			if (writer!=null) {
				writer.close();
			}
			if (out!=null) {
				out.close();
			}
		}			
	}	
	
	private boolean isValidAfcId(Long afcId) {
		if (afcId==null) {
			return false;
		}	
		Optional<AutomaticFrequencyControlProfile> afcProfile = afcService.findAutomaticFrequencyControlProfile(afcId);
		if (afcProfile.isPresent()) {
			return true;
		}	
		return false;
	}

}
