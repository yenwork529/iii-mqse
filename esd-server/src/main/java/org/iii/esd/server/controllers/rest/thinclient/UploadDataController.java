package org.iii.esd.server.controllers.rest.thinclient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.afc.service.AfcPerformanceService;
import org.iii.esd.api.request.thinclient.ThinClientAFCUploadDataResquest;
import org.iii.esd.api.request.thinclient.ThinClientFixDataResquest;
import org.iii.esd.api.request.thinclient.ThinClientUploadDataResquest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.DeviceReport;
import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.service.AutomaticFrequencyControlLogService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.server.services.CloudService;
import org.iii.esd.server.wrap.AfcLogWrapper;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.DeviceUtils;

import static org.iii.esd.api.RestConstants.REST_THINCLIENT_AFC_UPLOAD_DATA;
import static org.iii.esd.api.RestConstants.REST_THINCLIENT_FIX_DATA;
import static org.iii.esd.api.RestConstants.REST_THINCLIENT_UPLOAD_DATA;
import static org.iii.esd.utils.DatetimeUtils.isRealtimeData;

/**
 * TODO request async
 * TODO 是否要加上 fieldId是否啟用判斷? 或是在filter坐判斷 判斷IP和啟用是否合法
 */
@RestController
@Log4j2
public class UploadDataController {

    // 3分鐘內的資料才是即時資料
    private static final int realtimeMin = 3;
    private static final int AFC_PERFORMANCE_REGRESS_PERIOD = 2;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private FieldProfileService fieldProfileService;
    @Autowired
    private AutomaticFrequencyControlLogService automaticFrequencyControlLogService;
    @Autowired
    private AutomaticFrequencyControlService automaticFrequencyControlService;
    @Autowired
    private CloudService cloudService;
    @Autowired
    private AfcPerformanceService afcPerformanceService;


    /**
     * Field Data Upload
     * every hour on the 45th minute fix forecast data.
     * every hour on the 45th minute reschedule during 8:00~17:00. (eg 8:45~16:45)
     * 23:45 隔日調度排程資料先不做
     *
     * @param request
     */
    @PostMapping(REST_THINCLIENT_UPLOAD_DATA)
    public ApiResponse srUpload(@RequestBody ThinClientUploadDataResquest request) {

        Long fieldId = request.getFieldId();
        if (fieldId == null || fieldId < 1) {
            return new ErrorResponse(Error.invalidParameter, "FieldId is Required");
        }

        log.info(ToStringBuilder.reflectionToString(request));

        Optional<FieldProfile> oFieldProfile = fieldProfileService.find(fieldId);
        if (!oFieldProfile.isPresent()) {
            return new ErrorResponse(Error.invalidFieldId);
        } else {
            FieldProfile fieldProfile = oFieldProfile.get();
            if (EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
                return new ErrorResponse(Error.isNotEnabled, fieldId);
            } else {
                Date reportTime = new Date();
                fieldProfile.setTcLastUploadTime(reportTime);

                //fieldProfile.setConnectionStatus(DeviceUtils.checkConnectionStatus(reportTime));
                ElectricData currentSectionData = request.getCurrentSectionData();
                ElectricData realTimeElectricData = request.getRealTimeElectricData();
                List<DeviceReport> deviceReportDatas = request.getDeviceReportDatas();

                if (currentSectionData != null && isRealtimeData(currentSectionData.getTime(), 15)) {
                    FieldProfile _fieldProfile = currentSectionData.getFieldProfile();
                    if (_fieldProfile != null && !fieldId.equals(_fieldProfile.getId())) {
                        return new ErrorResponse(Error.invalidFieldId);
                    }
                    currentSectionData.setDataType(DataType.T1);
                    statisticsService.saveElectricData(currentSectionData);
                }

                if (realTimeElectricData != null && isRealtimeData(realTimeElectricData.getTime(), realtimeMin)) {
                    FieldProfile _fieldProfile = realTimeElectricData.getFieldProfile();
                    if (_fieldProfile != null && !fieldId.equals(_fieldProfile.getId())) {
                        return new ErrorResponse(Error.invalidFieldId);
                    }
                    realTimeElectricData.setDataType(DataType.T99);
                    statisticsService.saveElectricData(realTimeElectricData);
                }

                Date minReportTime = new Date(0);
                if (deviceReportDatas != null && deviceReportDatas.size() > 0) {
                    List<RealTimeData> realTimeDataList = deviceReportDatas.stream().
                            filter(dr -> isRealtimeData(dr.getReportTime(), realtimeMin)).
                                                                                   map(dr -> new RealTimeData(dr.getId(),
                                                                                           new DeviceProfile(dr.getId()),
                                                                                           dr.getReportTime(), dr.getMeasureData()))
                                                                           .collect(Collectors.toList());
                    minReportTime = new Date();
                    for (DeviceReport dr : deviceReportDatas) {
                        Date deviceReportTime = dr.getReportTime();
                        if (deviceReportTime != null) {
                            deviceService.updateConnectionStatusAndReportTimeById(deviceReportTime, dr.getId());
                            if (deviceReportTime.compareTo(minReportTime) == -1) {
                                minReportTime = deviceReportTime;
                            }
                        }
                    }
                    deviceService.saveRealTimeData(realTimeDataList);
                }
                fieldProfile.setDevStatus(DeviceUtils.checkConnectionStatus(minReportTime));
                fieldProfileService.update(fieldProfile);

                if (Calendar.getInstance().get(Calendar.MINUTE) == 45) {
                    try {
                        // 每小時第45分鐘
                        Date date = DatetimeUtils.add(DatetimeUtils.truncated(new Date(), Calendar.HOUR_OF_DAY), Calendar.MINUTE, 45);
                        // FIXME 目前還沒有可控設備 先註解
                        //cloudService.reSchedule(fieldId, date, false, isDispatchHours());
                    } catch (IiiException e) {
                        return new ErrorResponse(Error.getCode(e.getCode()), e.getMessage());
                    }
                }
                return new SuccessfulResponse();
            }
        }
    }

    /**
     * 補值
     *
     * @param request
     */
    @PostMapping(REST_THINCLIENT_FIX_DATA)
    public ApiResponse fix(@RequestBody ThinClientFixDataResquest request) {
        Long fieldId = request.getFieldId();
        log.debug(fieldId);
        if (fieldId == null || fieldId < 1) {
            return new ErrorResponse(Error.invalidParameter, "fieldId is Required");
        }
        List<ElectricData> list = request.getList();

        if (list != null && list.size() > 0) {
            list.forEach(ed -> {
                ed.setFieldProfile(new FieldProfile(fieldId));
                ed.setNeedFix(false);
                ed.init();
            });
            statisticsService.saveElectricData(list);
        }

        return new SuccessfulResponse();
    }

    @PostMapping(REST_THINCLIENT_AFC_UPLOAD_DATA+"_legacy")
    public ApiResponse afcUpload(@RequestBody ThinClientAFCUploadDataResquest request) {
        Long afcId = request.getId();
        if (afcId == null || afcId < 1) {
            return new ErrorResponse(Error.invalidParameter, "Id is Required");
        }

        Optional<AutomaticFrequencyControlProfile> oAfcProfile =
                automaticFrequencyControlService.findAutomaticFrequencyControlProfile(afcId);
        if (!oAfcProfile.isPresent()) {
            return new ErrorResponse(Error.invalidFieldId);
        } else {
            AutomaticFrequencyControlProfile afcProfile = oAfcProfile.get();
            if (EnableStatus.disable.equals(afcProfile.getEnableStatus())) {
                return new ErrorResponse(Error.isNotEnabled, afcId);
            } else {
                afcProfile.setTcLastUploadTime(new Date());
                List<ModbusMeter> list = request.getList();
                if (list != null && list.size() > 0) {
                    List<AutomaticFrequencyControlLog> afcLogs = list.stream()
                                                                     .map(m -> AfcLogWrapper.wrap(m, afcId))
                                                                     .collect(Collectors.toList());
                    automaticFrequencyControlLogService.addOrUpdateAll(afcId, afcLogs);
                    // 因每次計算都會使得最後一筆沒算到，故要減去回溯範圍 1 秒
                    afcPerformanceService.calculateSbspm(afcId,
                            regress(minTime(afcLogs), AFC_PERFORMANCE_REGRESS_PERIOD), maxTime(afcLogs));

                    // 拿掉上傳時計算 spm 的機制, 全部交由排程進行
                    // afcPerformanceService.calculateSpm(afcId, minTime(afcLogs), maxTime(afcLogs));
                }
                return new SuccessfulResponse();
            }
        }

    }

    private Date regress(Date date, int regress) {
        Instant time = date.toInstant();
        return Date.from(time.minus(regress, ChronoUnit.SECONDS));
    }

    private Date maxTime(List<AutomaticFrequencyControlLog> afcLogs) {
        return afcLogs.stream()
                      .max((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                      .orElseThrow(() -> new RuntimeException("time compare error"))
                      .getTimestamp();
    }

    private Date minTime(List<AutomaticFrequencyControlLog> afcLogs) {
        return afcLogs.stream()
                      .min((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                      .orElseThrow(() -> new RuntimeException("time compare error"))
                      .getTimestamp();
    }

}