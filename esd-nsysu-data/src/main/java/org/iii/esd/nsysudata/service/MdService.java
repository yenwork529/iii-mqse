package org.iii.esd.nsysudata.service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.iii.esd.Constants;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.ResponseStatus;
import org.iii.esd.mongo.document.AbstractMeasureData;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.vo.data.measure.MeterData;
import org.iii.esd.mongo.vo.data.setup.ISetupData;
import org.iii.esd.nsysudata.AppConfig;
import org.iii.esd.nsysudata.AppConfig.Device;
import org.iii.esd.nsysudata.vo.response.MdResponse;
import org.iii.esd.nsysudata.vo.response.MdResponse.Data;
import org.iii.esd.nsysudata.vo.response.MdResponse.Data.Value;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.thirdparty.service.notify.LineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MdService {
	
	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private DeviceService deviceService;

	private boolean isNotify = false;
	
	private Date time = new Date();
	
	@Autowired
	private LineService lineService;
	
	public void saveRealTimeData(String id, Device device) throws ParseException {
		ApiResponse response = getMessage(device.getFeed(), device.getChannel());
		if(response instanceof MdResponse && ResponseStatus.ok.equals(response.getStatus())) {
			saveRealTimeData(id, (MdResponse)response);
			if(isNotify) {
				lineService.sendMessage("nsysu-data catch date is recovery.");
				isNotify = false;
			}
			time = new Date();
		} else {
			log.info("time:{} isNotify:{}", time,  isNotify);
			if(!isNotify) {
				if((new Date().getTime()-time.getTime())>5*60*1000) {
					lineService.sendMessage("nsysu-data catch date failed: " + response.getErr().getMsg());					
					isNotify = true;
				}
			}
		}
	}

	private ApiResponse getMessage(String feed, String channel) {
		return httpService.formGet(
				MessageFormat.format("{0}feeds/{1}/channels/{2}/", appConfig.getUrl(), feed, channel), 
				MdResponse.class);
	}	

	private void saveRealTimeData(String id, MdResponse mdResponse) throws ParseException {
		Data data = mdResponse.getLatestValue();
		Value value = data.getValue();
		SimpleDateFormat sdf = Constants.ISO8601_FORMAT;
		sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		AbstractMeasureData abstractMeasureData = AbstractMeasureData.builder().
				deviceId(new DeviceProfile(id)).
				reportTime(sdf.parse(data.getAt())).
				measureData(MeterData.builder().
						activePower(new BigDecimal(value.getInst_kw())).
						kWh(new BigDecimal(value.getDel_total_kwh())).
						powerFactor(new BigDecimal(value.getInst_pf())).
						kVAR(new BigDecimal(value.getInst_kvar())).
						kVA(new BigDecimal(value.getInst_kva())).
						voltageA(new BigDecimal(value.getPhase_a_vol_v())).
						currentA(new BigDecimal(value.getPhase_a_cur_a())).
						voltageB(new BigDecimal(value.getPhase_b_vol_v())).
						currentB(new BigDecimal(value.getPhase_b_cur_a())).
						voltageC(new BigDecimal(value.getPhase_c_vol_v())).
						currentC(new BigDecimal(value.getPhase_c_cur_a())).
					build().wrap()).
				build();
		deviceService.report(abstractMeasureData);
	}
	
	public Map<String, String> getDeviceId() {
		Map<String, String> map = new HashMap<>();
		map.put(LoadType.M1.name(), check(appConfig.getMainLoad(), null));
		map.put(LoadType.M2.name(), check(appConfig.getSolarLoad(), appConfig.getPVSetupData()));
		return map;
	}

	private String check(Device device, ISetupData setupData) {
		Optional<DeviceProfile> dev = deviceService.findDeviceProfileById(device.getId());
    	if(dev.isPresent()){
    		return dev.get().getId();
    	}else {
    		DeviceProfile deviceProfile = new DeviceProfile();
    		deviceProfile.setId(device.getId());
    		deviceProfile.setName(device.getName());
    		deviceProfile.setFieldProfile(new FieldProfile(appConfig.getFieldId()));
    		deviceProfile.setDeviceType(DeviceType.Meter);
    		deviceProfile.setLoadType(LoadType.getCode(device.getLoadType()));
    		deviceProfile.setConnectionStatus(ConnectionStatus.Init);
    		deviceProfile.setISetupData(setupData);
    		deviceProfile.setUpdateTime(new Date());
    		return deviceService.add(deviceProfile);
    	}		
	}

}