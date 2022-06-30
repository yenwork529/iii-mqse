package org.iii.esd.collector.endpoint;

import org.iii.esd.mongo.service.integrate.IntegrateDataService;
// import org.iii.esd.mongo.service.integrate.TxgResourceDataService;
// import org.iii.esd.mongo.service.integrate.IntegrateHelperService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
// import org.iii.esd.server.scheduler.NewRemoteSensingJob;
// import org.iii.esd.collector.services.DnpUploader;
import java.util.Optional;
import java.util.List;
import org.iii.esd.enums.DataType;
import org.iii.esd.utils.DeviceUtils;

import java.util.stream.Collectors;
import org.iii.esd.api.vo.DeviceReport;

import static org.iii.esd.utils.DatetimeUtils.isRealtimeData;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;

// import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.iii.esd.api.RestConstants;
import org.iii.esd.api.request.thinclient.ThinClientAFCUploadDataResquest;
import org.iii.esd.api.request.thinclient.ThinClientUploadDataResquest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.exception.Error;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.collector.utils.Helper;
// import org.iii.esd.auth.vo.SignInRequest;
// import org.iii.esd.mongo.document.UserProfile;
// import org.iii.esd.mongo.service.UserService;
import org.iii.esd.mongo.document.*;
import org.iii.esd.mongo.document.integrate.CgenResData;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.SpinReserveService;
// import org.iii.esd.mongo.service.SimpleTxGroupService;
import org.iii.esd.utils.JsonUtils;
import org.iii.esd.utils.SiloOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.BindingResult;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.iii.esd.thirdparty.service.HttpService;
import lombok.extern.log4j.Log4j2;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// import javax.annotation.PostConstruct;

import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.DnpReportService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.StatisticsService;

@RestController
// @RequestMapping("/thinclient")
@Log4j2
public class UploadController {

	private static final int realtimeMin = 3;

	ExecutorService executorService = Executors.newFixedThreadPool(2);

	// @Autowired
	// private SimpleTxGroupService tgService;
	// private ITxGroupService txgService = TxGroupServiceFactory.getDefault();

	@Autowired
	private HttpService httpService;

	@Autowired
	// private UserService userService;
	private SpinReserveService spinServing;

	// @Autowired
	// private DeviceService deviceService;
	// @Autowired
	// private StatisticsService statisticsService;
	// @Autowired
	// private FieldProfileService fieldProfileService;
	// @Autowired
	// private DnpUploader dnpuploader;

	// @PostConstruct
	// void postcons() {
	// // fixConfig();
	// executorService = Executors.newFixedThreadPool(2);
	// }

	@Autowired
	IntegrateRelationService helperService;

	@RequestMapping(value = "/helo", method = { RequestMethod.GET }, produces = { "text/html" })
	public String epGetRes( //
			@RequestParam(value = "step", required = false, defaultValue = "9") Integer step, //
			@RequestParam(value = "aid", required = false, defaultValue = "1") String aid, //
			@RequestParam(value = "name", required = false, defaultValue = "index.html") String name, //
			@RequestParam(value = "renew", required = false, defaultValue = "false") boolean renew //
	) //
	{
		// TxgProfile t;

		// if (aid.equals("2")) {
		// 	dnpuploader.run();
		// 	return "OK";
		// }

		// switch (step) {
		// 	case 0:
		// 		helperService.purgeSampleConfiguration();
		// 		break;

		// 	case 1:
		// 		helperService.rebuildRelatins();
		// 		break;

		// 	case 2:
		// 		helperService.createSampleConfiguration();
		// 		break;

		// 	default:
		// 		break;
		// }
		// helperService.purgeSampleConfiguration();
		// helperService.createSampleConfiguration();
		// helperService.rebuildRelatins();
		// Long id = Long.parseLong(aid);
		// SpinReserveProfile sr = spinServing.findSpinReserveProfileFirst(id);
		// log.info(sr);
		// String url = // ApiConstant.buildAsp3Url(companyProfile.getDnpURL(), OperationType.AI,
		// 				// companyProfile.getTgCode());
		// 		"http://172.17.0.1:8585/asp3/ai/4";
		// // dnpuploader.processSpinReserve(id, url);
		// dnpuploader.run();
		// String so = aid + name;
		// log.info(so);
		return "OK";
	}

	@PostMapping(RestConstants.REST_THINCLIENT_UPLOAD_DATA_EX)
	// @PostMapping(RestConstants.REST_THINCLIENT_UPLOAD_DATA)
	public ApiResponse srupUpload( //
			@RequestBody String payload //
	// @RequestParam(value = "txguid", required = true) BigInteger txGuid, //
	// @RequestParam(value = "resid", required = true) Integer resId //
	) {
		// log.info("srup<<" + payload);

		return onSrupUpload(payload);

		// ThinClientUploadDataResquest req = JsonUtils.fromJson(payload, ThinClientUploadDataResquest.class);
		// log.info(req);

		// String so;
		// so = JsonUtils.toJson(req);
		// log.info(so);

		// Long fieldId = req.getFieldId();
		// if (fieldId == null || fieldId < 1) {
		// 	return new ErrorResponse(Error.invalidParameter, "FieldId is Required");
		// }

		// Optional<FieldProfile> oFieldProfile = fieldProfileService.find(fieldId);
		// if (!oFieldProfile.isPresent()) {
		// 	return new ErrorResponse(Error.invalidFieldId);
		// }

		// FieldProfile fieldProfile = oFieldProfile.get();
		// if (EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
		// 	return new ErrorResponse(Error.isNotEnabled, fieldId);
		// }

		// Date reportTime = new Date();
		// fieldProfile.setTcLastUploadTime(reportTime);

		// // fieldProfile.setConnectionStatus(DeviceUtils.checkConnectionStatus(reportTime));
		// ElectricData currentSectionData = req.getCurrentSectionData();
		// ElectricData realTimeElectricData = req.getRealTimeElectricData();
		// List<DeviceReport> deviceReportDatas = req.getDeviceReportDatas();

		// if (currentSectionData != null && isRealtimeData(currentSectionData.getTime(), 15)) {
		// 	FieldProfile _fieldProfile = currentSectionData.getFieldProfile();
		// 	if (_fieldProfile != null && !fieldId.equals(_fieldProfile.getId())) {
		// 		return new ErrorResponse(Error.invalidFieldId);
		// 	}
		// 	currentSectionData.setDataType(DataType.T1);
		// 	statisticsService.saveElectricData(currentSectionData);
		// }

		// if (realTimeElectricData != null && isRealtimeData(realTimeElectricData.getTime(), realtimeMin)) {
		// 	FieldProfile _fieldProfile = realTimeElectricData.getFieldProfile();
		// 	if (_fieldProfile != null && !fieldId.equals(_fieldProfile.getId())) {
		// 		return new ErrorResponse(Error.invalidFieldId);
		// 	}
		// 	realTimeElectricData.setDataType(DataType.T99);
		// 	statisticsService.saveElectricData(realTimeElectricData);
		// }

		// Date minReportTime = new Date(0);
		// if (deviceReportDatas != null && deviceReportDatas.size() > 0) {
		// 	List<RealTimeData> realTimeDataList = deviceReportDatas.stream()
		// 			.filter(dr -> isRealtimeData(dr.getReportTime(), realtimeMin))
		// 			.map(dr -> new RealTimeData(dr.getId(), new DeviceProfile(dr.getId()), dr.getReportTime(),
		// 					dr.getMeasureData()))
		// 			.collect(Collectors.toList());
		// 	minReportTime = new Date();
		// 	for (DeviceReport dr : deviceReportDatas) {
		// 		Date deviceReportTime = dr.getReportTime();
		// 		if (deviceReportTime != null) {
		// 			deviceService.updateConnectionStatusAndReportTimeById(deviceReportTime, dr.getId());
		// 			if (deviceReportTime.compareTo(minReportTime) == -1) {
		// 				minReportTime = deviceReportTime;
		// 			}
		// 		}
		// 	}
		// 	deviceService.saveRealTimeData(realTimeDataList);
		// }
		// fieldProfile.setDevStatus(DeviceUtils.checkConnectionStatus(minReportTime));
		// fieldProfileService.update(fieldProfile);


	}

	// @Autowired
	// NewRemoteSensingJob newRemoteSensingJbo;
	// @Scheduled(cron="10 * * * * *")
	// public void yyk(){
	// 	newRemoteSensingJbo.testProcess(1L);
	// }

	@PostMapping("/thinclient/as")
	public ApiResponse asUpload( //
			@RequestBody String payload, //
			@RequestParam(value = "txguid", required = true) BigInteger txGuid, //
			@RequestParam(value = "resid", required = true) Integer resId //
	) {
		// TransactionGroupProfile tp = tgService.findbyTxGuid(txGuid);
		// if (tp == null || !tp.containsResId(resId))
		// return new ErrorResponse(Error.invalidParameter);

		// if (tp.isDregType()) {
		// return onDregUpload(tp, resId, payload);
		// }

		ThinClientAFCUploadDataResquest data = Helper.fromJson(payload, ThinClientAFCUploadDataResquest.class);
		if (data == null)
			return new ErrorResponse(Error.invalidParameter);
		log.info(data);
		return new SuccessfulResponse();

		// Long fieldId = request.getFieldId();
		// if (fieldId == null || fieldId < 1) {
		// return new ErrorResponse(Error.invalidParameter, "FieldId is Required");
		// }

		// log.info(ToStringBuilder.reflectionToString(request));

		// Optional<FieldProfile> oFieldProfile = fieldProfileService.find(fieldId);
		// if (!oFieldProfile.isPresent()) {
		// return new ErrorResponse(Error.invalidFieldId);
		// } else {
		// FieldProfile fieldProfile = oFieldProfile.get();
		// if (EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
		// return new ErrorResponse(Error.isNotEnabled, fieldId);
		// } else {
		// Date reportTime = new Date();
		// fieldProfile.setTcLastUploadTime(reportTime);

		// //
		// fieldProfile.setConnectionStatus(DeviceUtils.checkConnectionStatus(reportTime));
		// ElectricData currentSectionData = request.getCurrentSectionData();
		// ElectricData realTimeElectricData = request.getRealTimeElectricData();
		// List<DeviceReport> deviceReportDatas = request.getDeviceReportDatas();

		// if (currentSectionData != null &&
		// isRealtimeData(currentSectionData.getTime(), 15)) {
		// FieldProfile _fieldProfile = currentSectionData.getFieldProfile();
		// if (_fieldProfile != null && !fieldId.equals(_fieldProfile.getId())) {
		// return new ErrorResponse(Error.invalidFieldId);
		// }
		// currentSectionData.setDataType(DataType.T1);
		// statisticsService.saveElectricData(currentSectionData);
		// }
		// if (realTimeElectricData != null &&
		// isRealtimeData(realTimeElectricData.getTime(), realtimeMin)) {
		// FieldProfile _fieldProfile = realTimeElectricData.getFieldProfile();
		// if (_fieldProfile != null && !fieldId.equals(_fieldProfile.getId())) {
		// return new ErrorResponse(Error.invalidFieldId);
		// }
		// realTimeElectricData.setDataType(DataType.T99);
		// statisticsService.saveElectricData(realTimeElectricData);
		// }

		// Date minReportTime = new Date(0);
		// if (deviceReportDatas != null && deviceReportDatas.size() > 0) {
		// List<RealTimeData> realTimeDataList = deviceReportDatas.stream()
		// .filter(dr -> isRealtimeData(dr.getReportTime(), realtimeMin))
		// .map(dr -> new RealTimeData(dr.getId(), new DeviceProfile(dr.getId()),
		// dr.getReportTime(),
		// dr.getMeasureData()))
		// .collect(Collectors.toList());
		// minReportTime = new Date();
		// for (DeviceReport dr : deviceReportDatas) {
		// Date deviceReportTime = dr.getReportTime();
		// if (deviceReportTime != null) {
		// deviceService.updateConnectionStatusAndReportTimeById(deviceReportTime,
		// dr.getId());
		// if (deviceReportTime.compareTo(minReportTime) == -1) {
		// minReportTime = deviceReportTime;
		// }
		// }
		// }
		// deviceService.saveRealTimeData(realTimeDataList);
		// }
		// fieldProfile.setDevStatus(DeviceUtils.checkConnectionStatus(minReportTime));
		// fieldProfileService.update(fieldProfile);

		// if (Calendar.getInstance().get(Calendar.MINUTE) == 45) {
		// try {
		// // 每小時第45分鐘
		// Date date = DatetimeUtils.add(DatetimeUtils.truncated(new Date(),
		// Calendar.HOUR_OF_DAY),
		// Calendar.MINUTE, 45);
		// // FIXME 目前還沒有可控設備 先註解
		// // cloudService.reSchedule(fieldId, date, false, isDispatchHours());
		// } catch (IiiException e) {
		// return new ErrorResponse(Error.getCode(e.getCode()), e.getMessage());
		// }
		// }
		// return new SuccessfulResponse();
		// }
		// }
	}

	String invalidateAfcRequest(ThinClientAFCUploadDataResquest request) {
		return null;
	}

	@PostMapping("/asfix")
	public ApiResponse retryAfcUpload(@RequestBody ThinClientAFCUploadDataResquest request) {
		String si = invalidateAfcRequest(request);
		if (si != null)
			return new ErrorResponse(Error.invalidParameter, si);

		return updateAfcDB(request);
	}

	@PostMapping("/asafc")
	public ApiResponse afcUpload(@RequestBody ThinClientAFCUploadDataResquest request) {
		String si = invalidateAfcRequest(request);
		if (si != null)
			return new ErrorResponse(Error.invalidParameter, si);

		submitDnpRequest(request);

		return updateAfcDB(request);
	}

	ApiResponse updateAfcDB(ThinClientAFCUploadDataResquest request) {
		return new SuccessfulResponse();
	}

	void submitDnpRequest(ThinClientAFCUploadDataResquest request) {
		// try { // http://140.92.27.13:8585/asp3/ai/9
		// String url = String.format("%s/asp3/ai/%d", //
		// SiloOptions.DnpUrl(), SiloOptions.TxGroup());
		// executorService.submit(new GenCallable() //
		// .setHttpService(httpService) //
		// .setDnpUrl(url) //
		// // .setResId(SiloOptions.ResourceId()) //
		// .setRequest(request));
		// } catch (Exception ex) {
		// log.error(ex.getMessage());
		// }
	}

	// @Autowired
	// TxgResourceDataService resourceService;

	@Autowired
	DnpReportService dnpReportService;

	@Autowired
	IntegrateDataService idataService;

		
	@PostMapping("/thinclient/dr")
	public ApiResponse onSrupUpload( //
			@RequestBody String payload //
	) {

		ThinClientUploadDataResquest req = Helper.fromJson(payload, ThinClientUploadDataResquest.class);

		if (req == null) {
			return new ErrorResponse(Error.invalidParameter);
		}

		ElectricData ed = req.getRealTimeElectricData();
		if (ed == null) {
			return new ErrorResponse(Error.invalidParameter);
		}

		DrResData dr = DrResData.from(req.getFieldMetaId(), ed);

		idataService.save(dr);

		return new SuccessfulResponse();

	}

}