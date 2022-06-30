package org.iii.esd.server.services;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.DataType;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.IiiException;
import org.iii.esd.exception.InvalidFieldIdException;
import org.iii.esd.exception.ScheduleException;
import org.iii.esd.forecast.service.FieldForecastService;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.schedule.HourlySchedulingService;
import org.iii.esd.schedule.SchedulingInputModel;
import org.iii.esd.utils.DatetimeUtils;

/***
 * 對應原C#版本ESD專案的Cloud相關API，<br>
 * 參考ESD\Service\Cloud\CloudService.cs進行改寫<br>
 * @author willhahn
 *
 */
@Service
@Log4j2
public class CloudService {

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private FieldForecastService fieldForecastService;

    @Autowired
    private HourlySchedulingService hourlySchedulingService;

    @Autowired
    private ElectricDataRepository electricDataRepository;

    /**
     * 重新排程產生T11資料
     *
     * @param fieldId
     * @param current
     * @param needGenData 是否產生預預測資料<br/>
     * @param needFixData 是否修正預測資料<br/>
     */
    public void reSchedule(Long fieldId, Date current, boolean needGenData, boolean needFixData) {
        Optional<FieldProfile> oFieldProfile = fieldProfileService.find(fieldId);
        if (!oFieldProfile.isPresent()) {
            throw new InvalidFieldIdException();
        } else {
            FieldProfile fieldProfile = oFieldProfile.get();
            // FIXME 若是之後還有其他可控設備要調整
            List<DeviceProfile> deviceProfileList = deviceService.findDeviceProfileByFieldIdAndLoadType(fieldId, LoadType.M3);

            if (deviceProfileList == null || deviceProfileList.size() == 0) {
                throw new IiiException(Error.theFieldHasNoControlDevice);
            } else {
                if (needGenData) {
                    // 進行全日預測產生T10資料
                    fieldForecastService.SaveFieldFullForecastElectricDataInDatabase(fieldId, DataType.T10, current);
                }
                if (needFixData) {
                    // 執行 PV / 負載預測校正
                    fieldForecastService.correctionInDatabase(fieldId, current, DataType.T1, DataType.T10);
                }
                // 產生T11資料
                hourlySchedule(fieldProfile, deviceProfileList, current);
            }
        }
    }

    /**
     * 回傳排程資料
     *
     * @param fieldId
     * @param current
     */
    public List<ElectricData> getScheduleData(Long fieldId, Date current) {
        List<ElectricData> electricDataList = getScheduleData(fieldId, current, Calendar.HOUR_OF_DAY);
        if (electricDataList == null || electricDataList.size() == 0) {
            current = DatetimeUtils.truncated(current, Calendar.DAY_OF_MONTH);
            reSchedule(fieldId, current, true, false);
            return getScheduleData(fieldId, current);
        }
        return electricDataList;
    }

    public List<ElectricData> getScheduleData(Long fieldId, Date current, int... calendarField) {
        return electricDataRepository.findByFieldIdAndDataTypeAndGreaterThanTime(fieldId, DataType.T11,
                calendarField.length > 0 ? DatetimeUtils.truncated(current, calendarField[0]) : current);
    }

    private void hourlySchedule(FieldProfile fieldProfile, List<DeviceProfile> deviceProfileList, Date current) {
        DeviceProfile essProfile = deviceProfileList.get(0);
        log.debug(fieldProfile.getId() + " 進行HOURLY排程(" + current + ")");
        try {
            SchedulingInputModel model = new SchedulingInputModel();
            model.setSchedule_Start(current);
            model.setFieldId(fieldProfile.getId());
            model.setFORECAST_DATA_TYPE(DataType.T10.getCode());
            model.setREAL_DATA_TYPE(DataType.T1.getCode());
            model.setSCHEDULE_DATA_TYPE(DataType.T11.getCode());
            model.setBattery_Capacity(essProfile.getSetupData().getCapacity());
            double chargeKw = essProfile.getSetupData().getChargeKw().doubleValue(); // 充電最大功率
            double dischargeKw = essProfile.getSetupData().getDischargeKw().doubleValue(); // 放電最大功率
            model.setMax_Power(BigDecimal.valueOf(Math.max(chargeKw, dischargeKw))); // 充電最大功率和放電最大功率，兩者取大
            model.setCharge_Efficiency(essProfile.getSetupData().getChargeEfficiency());
            model.setDischarge_Efficiency(essProfile.getSetupData().getDischargeEfficiency());
            model.setTYOD(BigDecimal.valueOf(fieldProfile.getTyod()));
            model.setConfig(PolicyProfile.Default());
            model.check();
            hourlySchedulingService.SchedulingByStrategy(model);
            log.debug(fieldProfile.getId() + " 進行HOURLY排程(" + current + ")結束");
        } catch (Exception ex) {
            log.error("Hourly排程失敗, fieldId={}, exception={}", fieldProfile.getId(), ex.getMessage());
            throw new ScheduleException(Error.scheduleFailed);
        }
    }

}