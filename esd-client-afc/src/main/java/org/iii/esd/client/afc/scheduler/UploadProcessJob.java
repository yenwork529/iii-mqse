package org.iii.esd.client.afc.scheduler;

import static org.iii.esd.api.RestConstants.REST_THINCLIENT_AFC_UPLOAD_DATA;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.iii.esd.afc.performance.PMcalSbspm;
import org.iii.esd.annotation.LogExecutionTime;
import org.iii.esd.api.request.thinclient.ThinClientAFCUploadDataResquest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.ErrorDetail;
import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.client.afc.AFCConfig;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@ConfigurationProperties
@Log4j2
public class UploadProcessJob {

    @Autowired
    protected AFCConfig config;

	@Autowired
	protected AutomaticFrequencyControlService afcService;

    @Value("${upload.server}")
    private String server;

    @Value("${upload.dnp}")
    private String dnp;    

	@Autowired
	protected PMcalSbspm sbspmModule;

	@Autowired
	private HttpService httpService;

	@LogExecutionTime
	@Async("uploadExecutor")
	public Future<Void> run() throws InterruptedException {
		log.debug("start");
		Date end = DatetimeUtils.add(new Date(), Calendar.SECOND, -1);
		Long id = config.getAfcId();
		List<AutomaticFrequencyControlLog> list = afcService.findAutomaticFrequencyControlLogByIdAndTime(id, DatetimeUtils.add(end, Calendar.SECOND, -10), end);
		//log.info(end + " " + list.size());
		if(list!=null) {
			int size = list.size();
			if(size>0) {
				if(size<10) {
					log.warn("upload data is missing.");
				}
				ThinClientAFCUploadDataResquest request = new ThinClientAFCUploadDataResquest(id, 
						list.stream().map(log->new ModbusMeter(log)).collect(Collectors.toList()));
				if(size>2) {
					AutomaticFrequencyControlLog afclog = list.get(size-2);
					request.setSbspmTime(afclog.getTimestamp());
//					request.setSbspm(new PMcalSbspm(
//							new Double[] {list.get(size-3).getFrequency().doubleValue(), afclog.getFrequency().doubleValue()}, 
//							new Double[] {afclog.getEssPowerRatio().doubleValue(), list.get(size-1).getEssPowerRatio().doubleValue()}
//						).calculate());		
					sbspmModule.setFrequencies(new Double[] {list.get(size-3).getFrequency().doubleValue(), afclog.getFrequency().doubleValue()});
					sbspmModule.setActualPowerRatios(new Double[] {afclog.getEssPowerRatio().doubleValue(), list.get(size-1).getEssPowerRatio().doubleValue()});
					request.setSbspm(sbspmModule.calculate());
				}else {
					log.error("sbspm isn`t not calculated.");
				}
				uploadData(dnp, request);
				uploadData(server.concat(REST_THINCLIENT_AFC_UPLOAD_DATA), request);
			}
		}
		return new AsyncResult<Void>(null);
	}

	private ApiResponse uploadData(String url, ThinClientAFCUploadDataResquest request) {
		ApiResponse res = httpService.jsonPost(url, request, SuccessfulResponse.class);
		if(checkError(res)) {
			log.error("{} Upload Data Failed!!", url);
		}
		return res;
	}	
	
	private boolean checkError(ApiResponse res) {
		boolean hasError = true;
		if (res != null) {
			ErrorDetail errorDetail = res.getErr();
			if (!(res instanceof ErrorResponse) && (errorDetail != null && 0 == errorDetail.getCode())) {
				hasError = false;
			} else {
				log.error(errorDetail);
			}
		}
		return hasError;
	}
	
}