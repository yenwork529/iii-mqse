package org.iii.esd.client.service;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.api.RestConstants;
import org.iii.esd.api.request.thinclient.ThinClientFixDataResquest;
import org.iii.esd.api.request.thinclient.ThinClientRegisterResquest;
import org.iii.esd.api.request.thinclient.ThinClientUploadDataResquest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.response.thinclient.FieldDataResponse;
import org.iii.esd.api.response.thinclient.ScheduleResponse;
import org.iii.esd.api.vo.DeviceReport;
import org.iii.esd.api.vo.ErrorDetail;
import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.*;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.PolicyProfileService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.EncryptUtils;
import org.iii.esd.utils.JsonUtils;
import org.iii.esd.utils.SiloOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ClientService {

    @Autowired
    private HttpService httpService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private PolicyProfileService policyProfileService;

    @Autowired
    private StatisticsService statisticsService;

    // @Value("${cloudUrl}")
    // private String url;

    // @Value("${metaId}")
    // private String metaId;

    public ApiResponse callRegister(ThinClientRegisterResquest req) {
        ApiResponse res = httpService.formPost(SiloOptions.ServerUrl().concat(RestConstants.REST_THINCLIENT_REGISTER),
                req, SuccessfulResponse.class);

        if (isError(res)) {
            log.error("FieldId:{} Register Failed!!", req.getFieldId());
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    public <T extends ApiResponse> T syncField(FieldProfile fieldProfile) {
        ApiResponse resp = syncFieldDataFromServer(fieldProfile);

        if (isValid(resp)) {
            FieldDataResponse fieldDataResponse = (FieldDataResponse) resp;
            fieldProfileService.update(fieldDataResponse.getField());

            Optional<PolicyProfile> policyProfile = Optional.ofNullable(fieldDataResponse.getPolicy());
            policyProfile.ifPresent(profile -> policyProfileService.update(profile));

            List<DeviceProfile> serverDevices = fieldDataResponse.getDevices();
            serverDevices.forEach(this::saveTcDevice);
        } else {
            log.error("FieldId:{} Sync Field Data Failed!!", fieldProfile.getId());
        }

        return (T) resp;
    }

    private void saveTcDevice(DeviceProfile serverDevice) {
        Optional<DeviceProfile> otcDevice = deviceService.findDeviceProfileById(serverDevice.getId());

        if (otcDevice.isPresent()) {
            DeviceProfile tcDevice = otcDevice.get();
            tcDevice.setName(serverDevice.getName());
            tcDevice.setLoadType(serverDevice.getLoadType());
            tcDevice.setSetupData(serverDevice.getSetupData());
            tcDevice.setEnableStatus(serverDevice.getEnableStatus());
            tcDevice.setMainLoad(serverDevice.isMainLoad());

            deviceService.saveDeviceProfile(tcDevice);
        } else {
            log.warn("DeviceId:{} is not exist!", serverDevice.getId());
        }
    }

    private ApiResponse syncFieldDataFromServer(FieldProfile fieldProfile) {
        return httpService.authorizationGet(
                replace(SiloOptions.ServerUrl().concat(RestConstants.REST_THINCLIENT_SYNC_FIELD_DATA), fieldProfile),
                genToken(fieldProfile), null, FieldDataResponse.class);
    }

    /**
     * @param fieldProfile
     * @param delay
     * @param minute
     */
    public ApiResponse uploadData(FieldProfile fieldProfile, int delay, int minute) {
        Long fieldId = fieldProfile.getId();
        ThinClientUploadDataResquest req = new ThinClientUploadDataResquest(fieldId);
        req.setFieldMetaId(SiloOptions.FiledMetaId(fieldId));

        Optional<ElectricData> ed1 = getElectricData(fieldId, DataType.T99, delay);
        if (ed1.isPresent()) {
            req.setRealTimeElectricData(ed1.get());
        } else {
            log.warn("fieldId:{} RealTimeElectricData is null in delay {}", fieldId, delay);
        }

        if (isT1Time(minute, delay)) {
            Optional<ElectricData> ed = getElectricData(fieldId, DataType.T1, delay);
            if (ed.isPresent()) {
                req.setCurrentSectionData(ed.get());
            } else {
                log.warn("fieldId:{} CurrentSectionData is null", fieldId);
            }
        }

        List<RealTimeData> realTimeData = deviceService.findRealTimeDataByFieldId(fieldId);
        req.setDeviceReportDatas(realTimeData.stream().map(rtd -> DeviceReport.builder().id(rtd.getId())
                .measureData(rtd.getMeasureData()).reportTime(rtd.getReportTime()).build())
                .collect(Collectors.toList()));

        String url, si; ApiResponse res;
        if(SiloOptions.isIntegrated()){
            url = SiloOptions.AgentUrl().concat(RestConstants.REST_THINCLIENT_UPLOAD_DATA_EX);
            log.info("send to>>" + url);
            si = JsonUtils.toJson(req);
            log.info(si);
            res = httpService.authorizationJsonPost(url, genToken(fieldProfile), si, SuccessfulResponse.class);
        }
        else {
            url = SiloOptions.ServerUrl().concat(RestConstants.REST_THINCLIENT_UPLOAD_DATA);
            log.info("send to>>" + url);
            res = httpService.authorizationJsonPost(url, genToken(fieldProfile), req, SuccessfulResponse.class);
        }


        if (isValid(res)) {
            // 上傳成功，則修改device同步狀態
            deviceService.updateIsSyncByFieldId(true, fieldProfile.getId());
        } else {
            log.error("FieldId:{} Upload Data Failed!!", fieldProfile.getId());
        }

        return res;
    }

    private boolean isT1Time(int minute, int delay) {
        return minute % 15 == delay;
    }

    @SuppressWarnings("unchecked")
    public <T extends ApiResponse> T schedule(FieldProfile fieldProfile) {
        ApiResponse res = httpService.authorizationGet(
                replace(SiloOptions.ServerUrl().concat(RestConstants.REST_THINCLIENT_SCHEDULE), fieldProfile),
                genToken(fieldProfile), null, ScheduleResponse.class);
        if (isError(res)) {
            log.error("FieldId:{} Get Schedule Data Failed!!", fieldProfile.getId());
        } else {
            statisticsService.saveElectricData(((ScheduleResponse) res).getElectricData());
        }
        return (T) res;
    }

    @SuppressWarnings("unchecked")
    public <T extends ApiResponse> T reschedule(FieldProfile fieldProfile, int delay, int minute) {
        Long fieldId = fieldProfile.getId();
        ThinClientUploadDataResquest req = new ThinClientUploadDataResquest(fieldId);
        if (minute % 15 == delay) {
            ElectricData ed = getLastElectricData(fieldId, DataType.T1, delay);
            if (ed != null) {
                req.setCurrentSectionData(ed);
            } else {
                log.warn("fieldId:{} CurrentSectionData is null", fieldId);
            }
        }

        ApiResponse res = httpService.authorizationJsonPost(
                SiloOptions.ServerUrl().concat(RestConstants.REST_THINCLIENT_RESCHEDULE), genToken(fieldProfile), req,
                ScheduleResponse.class);
        if (isError(res)) {
            log.error("FieldId:{} Reschedule Failed!!", fieldProfile.getId());
        } else {
            statisticsService.saveElectricData(((ScheduleResponse) res).getElectricData());
        }
        return (T) res;
    }

    public ApiResponse fix(ThinClientFixDataResquest req, FieldProfile fieldProfile) {
        ApiResponse res = httpService.authorizationJsonPost(
                SiloOptions.ServerUrl().concat(RestConstants.REST_THINCLIENT_FIX_DATA), genToken(fieldProfile), req,
                SuccessfulResponse.class);
        if (isError(res)) {
            log.error("fieldId:{} Fix Data Failed!!", req.getFieldId());
        }
        return res;
    }

    private boolean isValid(ApiResponse res) {
        return !isError(res);
    }

    private boolean isError(ApiResponse res) {
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

    private Optional<ElectricData> getElectricData(long fieldId, DataType dataType, int delay) {
        Date time = DatetimeUtils.add(DatetimeUtils.getNowWithoutSec(), Calendar.MINUTE, -delay);

        log.info("get electric data of type {} of field {} from {} ago.", dataType, fieldId, time);

        return statisticsService.findElectricData(fieldId, dataType, time);
    }

    private ElectricData getLastElectricData(long fieldId, DataType dataType, int delay) {

        Date time = DatetimeUtils.add(DatetimeUtils.getNowWithoutSec(), Calendar.MINUTE, -delay);

        return statisticsService.findElectricData(fieldId, dataType, time).orElseGet(() -> {
            log.warn("this time:{} no data ", time);
            return null;
        });
    }

    private String genToken(FieldProfile fieldProfile) {
        // log.info(EncryptUtils.md5Encode(fieldProfile.getId()+"|"+fieldProfile.getTcIp()));
        return EncryptUtils.genThinClientToken(fieldProfile.getId(), fieldProfile.getTcIp());
    }

    private String replace(String url, FieldProfile fieldProfile) {
        return url.replace(RestConstants.PATHVARIABLE_FIELDID, fieldProfile.getId().toString());
    }

}