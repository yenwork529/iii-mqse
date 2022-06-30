package org.iii.esd.client.controllers.rest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.request.gateway.GatewayDataUploadRequest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.gateway.ChannelData;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.AbstractMeasureData;
import org.iii.esd.mongo.document.DeviceHistory;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.vo.data.measure.MeasureData;
import org.iii.esd.mongo.vo.data.setup.SetupData;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.DeviceUtils;
import org.iii.esd.utils.JsonUtils;

@RestController
@Log4j2
public class GatewayController {

    @Autowired
    private DeviceService deviceService;

    /**
     * Gateway 資料上傳，補值
     * feedId 為 parent deviceId 長度為20碼 ex: II10DTIDEMO---------
     * channelId 為 deviceId 長度為20碼 ex: II10DTIDEMO-------01
     *
     * @param request
     * @param feedId
     * @param channelId
     * @param requestData
     */
    static boolean _debug_on = false;
    static int _debug_enters = 0;
    @PostMapping(value = "/feeds/{feedId}/channels/{channelId}/data/insert",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse upload( //
            HttpServletRequest request,
            @PathVariable String feedId,
            @PathVariable String channelId,
            @RequestBody GatewayDataUploadRequest<ChannelData> requestData) { //
        if(_debug_on && _debug_enters > 0){
            return new ErrorResponse(Error.insertDataError);
        }
        _debug_enters = 1;
        ApiResponse ret = do_upload(request, feedId, channelId, requestData);
        _debug_enters = 0;
        return ret;
    }
    ApiResponse do_upload(HttpServletRequest request,
        String feedId, String channelId, 
        GatewayDataUploadRequest<ChannelData> requestData    ){

        String si = String.format("/feeds/%s/channels/%s/data/insert", feedId, channelId);
        log.info(si + " enters");
        // log.info(JsonUtils.toJson(requestData));
        List<ChannelData> channelDataList = requestData.getPayload().getResources();

        if (isNotInsertDataRequest(requestData) || CollectionUtils.isEmpty(channelDataList)) {
            return new ErrorResponse(Error.insertDataError);
        }

        try {
            DeviceProfile deviceProfile = buildDeviceProfile(feedId, channelId);

            Optional<DeviceProfile> oDeviceProfile = deviceService.findDeviceProfileById(channelId);
            if (oDeviceProfile.isPresent()) {
                deviceProfile = oDeviceProfile.get();
            } else {
                createDeviceProfile(deviceProfile, feedId, channelId);
            }

            if (EnableStatus.isEnabled(deviceProfile.getEnableStatus())) {
                Supplier<Stream<ChannelData>> supplier = channelDataList::stream;

                ChannelData lastChannelData = supplier.get()
                                                      .max(Comparator.comparing(ChannelData::getAt,
                                                              Comparator.nullsFirst(
                                                                      Comparator.naturalOrder())))
                                                      .get();
                Date reportTime = lastChannelData.getAt();
                Date oldReportTime = deviceProfile.getReportTime();

                //log.info(reportTime + " "+ lastChannelData.getValue().get("del_total_kwh"));
                // 所有上傳資料最新的比目前的新才需更新，或是本來無資料
                if (oldReportTime == null || reportTime.after(oldReportTime)) {
                    deviceProfile.setReportTime(reportTime);

                    // 更新即時資料
                    AbstractMeasureData amd = transformToMeasureData(lastChannelData, deviceProfile);
                    if (amd != null) {
                        deviceService.saveRealTimeData(new RealTimeData(amd));
                        deviceProfile.setSync(false);
                    }
                }

                deviceProfile.setConnectionStatus(DeviceUtils.checkConnectionStatus(reportTime));

                // 更新DeviceProfile
                final DeviceProfile dp = deviceService.saveDeviceProfile(deviceProfile);
                List<AbstractMeasureData> abstractMeasureDataList = supplier.get()
                                                                            .map(cd -> transformToMeasureData(cd, dp))
                                                                            .filter(Objects::nonNull)
                                                                            .collect(Collectors.toList());
                // 寫入歷史資料
                deviceService.saveDeviceHistory(abstractMeasureDataList.stream()
                                                                       .map(DeviceHistory::new)
                                                                       .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        log.info(si + " leavs");
        return new SuccessfulResponse();
    }

    private void createDeviceProfile(DeviceProfile deviceProfile, String feedId, String channelId) {
        DeviceProfile parent = deviceProfile.getParent();

        if (parent.getId() == null) {
            parent.setId(feedId);
            parent.setEnableStatus(EnableStatus.enable);
            deviceService.add(parent);
        }

        deviceProfile.setId(channelId);
        deviceProfile.setEnableStatus(EnableStatus.enable);

        deviceService.add(deviceProfile);
    }

    private boolean isNotInsertDataRequest(GatewayDataUploadRequest<ChannelData> requestData) {
        return !"insertDataRequest".equals(requestData.getKind());
    }

    private DeviceProfile buildDeviceProfile(String feedId, String channelId) {
        return DeviceProfile.builder()
                            .id(channelId)
                            .deviceType(DeviceType.getCodeByChannelId(channelId))
                            .parent(DeviceProfile.getInstanceByFeedId(feedId))
                            .isSync(false)
                            .build();
    }

    private AbstractMeasureData transformToMeasureData(ChannelData channelData, DeviceProfile deviceProfile) {
        MeasureData measureData = convertToMeasureData(channelData, deviceProfile);

        if (measureData != null) {
            BigDecimal activePower = measureData.getActivePower();

            if (activePower != null && (activePower.intValue() > 60000 || activePower.intValue() < -60000)) {
                log.warn("activePower value is abnormal:{}", activePower);

                DeviceHistory deviceHistory = deviceService.findLastDeviceHistoryByDeviceIdAndTime(
                        deviceProfile.getId(),
                        getLastMinute(channelData.getAt()));

                if (deviceHistory != null
                        && deviceHistory.getMeasureData().getActivePower() != null) {
                    measureData.setActivePower(deviceHistory.getMeasureData()
                                                            .getActivePower());
                } else {
                    return null;
                }
            }
        }

        return AbstractMeasureData.builder()
                                  .deviceId(deviceProfile)
                                  .reportTime(channelData.getAt())
                                  .measureData(ratio(measureData, deviceProfile))
                                  .build();
    }

    private Date getLastMinute(Date at) {
        return DatetimeUtils.add(at, Calendar.MINUTE, -1);
    }

    private MeasureData convertToMeasureData(ChannelData channelData, DeviceProfile deviceProfile) {
        ObjectMapper mapper = buildMapper();
        MeasureData measureData =
                mapper.convertValue(
                        channelData.getValue(),
                        deviceProfile.getDeviceType()
                                     .getClazz())
                      .wrap();
        // double ob;
        // ob = (double)channelData.getValue().get("energyIMP");
        measureData.setEnergyImp(new BigDecimal(channelData.getValue().get("energyIMP").toString()));
        // ob = (double)channelData.getValue().get("energyEXP");
        measureData.setEnergyExp(new BigDecimal(channelData.getValue().get("energyEXP").toString()));
        return measureData;
    }

    private ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    private MeasureData ratio(MeasureData measureData, DeviceProfile deviceProfile) {
        if (DeviceType.isNotBattery(deviceProfile.getDeviceType())) {
            SetupData setupData = deviceProfile.getSetupData();

            if (setupData != null) {
                BigDecimal pt = setupData.getPt();
                BigDecimal ct = setupData.getCt();

                if (pt != null && ct != null) {
                    measureData.ratio(pt, ct);
                }
            }
        }
        return measureData;
    }

}