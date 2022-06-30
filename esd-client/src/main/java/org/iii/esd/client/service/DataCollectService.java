package org.iii.esd.client.service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.iii.esd.Constants;
import org.iii.esd.api.request.thinclient.ThinClientFixDataResquest;
import org.iii.esd.caculate.Utility;
import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.DeviceHistory;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.mongo.vo.data.measure.MeasureData;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.SiloOptions;

@Service
@Log4j2
public class DataCollectService {

    // @Value("${metaId}")
    // private String metaId;

    @Value("${globalScale}")
    private int globalScale;

    @Value("${catchBuffer}")
    private int catchBuffer;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ClientService clientService;

    public void runRealTime(Long fieldId, Date end, int delay, List<DeviceProfile> list) {
        Date start1 = DatetimeUtils.add(end, Calendar.MINUTE, -1 - delay);
        Date start15 = DatetimeUtils.add(end, Calendar.MINUTE, -15 - delay);

        BigDecimal totalActivePower = BigDecimal.ZERO;
        BigDecimal totalKWh = BigDecimal.ZERO;
        BigDecimal energyImp = BigDecimal.ZERO;
        BigDecimal energyExp = BigDecimal.ZERO;

        for (DeviceProfile deviceProfile : list) {
            if (deviceProfile.isMainLoad()) {
                if (ConnectionStatus.Connected.equals(deviceProfile.getConnectionStatus())) {
                    Optional<RealTimeData> ortd = deviceService.findRealTimeDataById(deviceProfile.getId());
                    if (ortd.isPresent()) {
                        RealTimeData rtd = ortd.get();
                        log.info("calculating realtime data {}", rtd);

                        BigDecimal ap = rtd.getMeasureData().getActivePower();
                        if (ap != null) {
                            totalActivePower = totalActivePower.add(ap);
                        }

                        BigDecimal kWh = rtd.getMeasureData().getKWh();
                        if (kWh != null) {
                            totalKWh = totalKWh.add(kWh);
                        }

                        BigDecimal bd;
                        bd = rtd.getMeasureData().getEnergyImp();
                        if(bd != null){
                            energyImp = energyImp.add(bd);
                        }
                        bd = rtd.getMeasureData().getEnergyExp();
                        if(bd != null){
                            energyExp = energyExp.add(bd);
                        }
                    } else {
                        log.warn("realtime data do not exist.");
                    }
                } else {
                    log.warn("DeviceId:{} Mainload is not Connected.", deviceProfile.getId());
                }
            } else {
                log.warn("escape by not main load");
            }
        }

        Map<LoadType, List<String>> ltMap = getLoadTypeMap(list);

        if (isT1Time(delay)) {
            log.info("calculate load type T1.");
            calculate(ltMap, fieldId, DataType.T1, start15, end, null, null, null, null);
        }

        log.info("calculate load type T99");
        calculate(ltMap, fieldId, DataType.T99, start1, end, totalActivePower, totalKWh, energyImp, energyExp);
    }

    private boolean isT1Time(int delay) {
        return Calendar.getInstance().get(Calendar.MINUTE) % 15 == delay;
    }

    public void runFix(Long fieldId, Date start, Date end) {
        List<DeviceProfile> list = deviceService.findDeviceProfileByFieldId(fieldId);
        if (list.size() > 0) {
            List<ElectricData> t99List = calculator(fieldId, list, start, end, DataType.T99);
            List<ElectricData> t1List = calculator(fieldId, list, start, end, DataType.T1);
            List<ElectricData> dataList = new ArrayList<>();
            dataList.addAll(t99List);
            dataList.addAll(t1List);
            ThinClientFixDataResquest req = new ThinClientFixDataResquest(fieldId, dataList);
            req.setFieldMetaId(SiloOptions.FiledMetaId(fieldId));
            clientService.fix(req, new FieldProfile(fieldId));
        }
    }

    private Map<LoadType, List<String>> getLoadTypeMap(List<DeviceProfile> list) {
        List<String> m1IdList = new ArrayList<>();
        List<String> m2IdList = new ArrayList<>();
        List<String> m3IdList = new ArrayList<>();
        List<String> m5IdList = new ArrayList<>();
        List<String> m6IdList = new ArrayList<>();
        List<String> m8IdList = new ArrayList<>();
        List<String> m10IdList = new ArrayList<>();

        for (DeviceProfile deviceProfile : list) {
            LoadType loadType = deviceProfile.getLoadType();
            if (loadType != null) {
                switch (deviceProfile.getLoadType()) {
                    case M1:
                        m1IdList.add(deviceProfile.getId());
                        break;
                    case M2:
                        m2IdList.add(deviceProfile.getId());
                        break;
                    case M3:
                        m3IdList.add(deviceProfile.getId());
                        break;
                    case M5:
                        m5IdList.add(deviceProfile.getId());
                        break;
                    case M6:
                        m6IdList.add(deviceProfile.getId());
                        break;
                    case M8:
                        m8IdList.add(deviceProfile.getId());
                        break;
                    case M10:
                        m10IdList.add(deviceProfile.getId());
                        break;
                    default:
                        break;
                }
            } else {
                log.warn("DeviceId:{} loadType is null.", deviceProfile.getId());
            }
        }

        Map<LoadType, List<String>> ltMap = new HashMap<>();
        ltMap.put(LoadType.M1, m1IdList);
        ltMap.put(LoadType.M2, m2IdList);
        ltMap.put(LoadType.M3, m3IdList);
        ltMap.put(LoadType.M5, m5IdList);
        ltMap.put(LoadType.M6, m6IdList);
        ltMap.put(LoadType.M8, m8IdList);
        ltMap.put(LoadType.M10, m10IdList);

        return ltMap;
    }

    private void calculate(Map<LoadType, List<String>> ltMap, Long fieldId, DataType dataType, Date start, Date end, BigDecimal activePower,
            BigDecimal totalkWh, BigDecimal energyImp, BigDecimal energyExp) {
        ElectricData electricData =
                ElectricData.builder()
                            .fieldProfile(new FieldProfile(fieldId))
                            .dataType(dataType)
                            .time(end)
                            .activePower(activePower)
                            .totalkWh(totalkWh)
                            .build();

        boolean needFix = false;
        List<String> needFixIdList = new ArrayList<>();

        for (LoadType loadType : ltMap.keySet()) {
            List<String> idList = ltMap.get(loadType);

            if (idList.size() > 0) {
                BigDecimal value = calculateLoad15KW(idList, start, end, loadType, dataType);

                if (value == null) {
                    needFix = true;
                    needFixIdList.addAll(idList);
                }

                setValue(electricData, loadType.getCode(), value);
            }
        }

        if (needFix) {
            log.warn("DeviceId:{} at this time is not report in FieldId:{}", needFixIdList, fieldId);
        }

        electricData.setNeedFix(needFix);
        electricData.init(globalScale);
        electricData.setEnergyImp(energyImp);
        electricData.setEnergyExp(energyExp);

        statisticsService.saveElectricData(electricData);
    }

    public void setValue(ElectricData electricData, String code, BigDecimal value) {
        try {
            PropertyUtils.setProperty(electricData, String.format("m%skW", code), value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage());
        }
    }

    private List<ElectricData> calculator(Long fieldId, List<DeviceProfile> list, Date start, Date end, DataType dataType) {
        List<List<ElectricData>> dataList = new ArrayList<>();
        for (DeviceProfile deviceProfile : list) {
            dataList.add(calculator15KW(deviceProfile, start, end, dataType));
        }
        long count = Duration.between(start.toInstant(), end.toInstant()).toMinutes() / dataType.getInterval();
        List<ElectricData> mergeList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ElectricData ed = new ElectricData();
            for (List<ElectricData> el : dataList) {
                ed = ed.sum(el.get(i));
                ed.setDataType(dataType);
                ed.setFieldProfile(new FieldProfile(fieldId));
                ed.init(3);
            }
            mergeList.add(ed);
        }
        return statisticsService.saveElectricData(mergeList);
    }

    private BigDecimal calculateLoad15KW(List<String> idList, Date start, Date end, LoadType loadType, DataType dataType) {
        // 秒級資料先不處理
        if (idList.size() > 0 && !DataType.TS.equals(dataType)) {
            List<BigDecimal> list = idList.stream()
                                          .map(id -> calculator15KW(id, start, end, loadType))
                                          .filter(Objects::nonNull).collect(Collectors.toList());
            return list.size() > 0 ? list.stream().reduce(BigDecimal.ZERO, BigDecimal::add) : null;
        } else {
            return null;
        }
    }

    private List<ElectricData> calculator15KW(DeviceProfile deviceProfile, Date start, Date end, DataType dataType) {
        List<ElectricData> result = new ArrayList<>();

        List<DeviceHistory> historyList = deviceService.findDeviceHistoryByDeviceIdAndTime(
                deviceProfile.getId(),
                DatetimeUtils.add(start, Calendar.SECOND, -catchBuffer),
                DatetimeUtils.add(end, Calendar.SECOND, catchBuffer));
        int intervalSeconds = dataType.getInterval();

        Date cursor = start;
        while (cursor.before(end)) {
            Date cursorStart = cursor;
            Date cursorEnd = DatetimeUtils.add(cursorStart, Calendar.SECOND, intervalSeconds * 60);
            List<DeviceHistory> historyInPeriod =
                    historyList.stream()
                               .filter(
                                       history -> history.getReportTime()
                                                         .after(DatetimeUtils.add(cursorStart, Calendar.SECOND, -1))
                                               && history.getReportTime()
                                                         .before(DatetimeUtils.add(cursorEnd, Calendar.SECOND, 1)))
                               .collect(Collectors.toList());

            if (!historyInPeriod.isEmpty()) {
                int lastIndex = historyInPeriod.size() - 1;
                ElectricData electricData = ElectricData.builder()
                                                        .dataType(dataType)
                                                        .needFix(true)
                                                        .time(cursorEnd)
                                                        .build();

                if (historyInPeriod.size() > 1) {
                    DeviceHistory dhStart = historyInPeriod.get(0);
                    DeviceHistory dhEnd = historyInPeriod.get(lastIndex);

                    try {
                        LoadType loadType = deviceProfile.getLoadType();
                        PropertyUtils.setProperty(electricData, String.format("m%skW", loadType.getCode()),
                                calculator15KW(dhStart, dhEnd, loadType));
                        electricData.setNeedFix(false);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        log.error(ExceptionUtils.getStackTrace(e));
                    }
                }

                if (deviceProfile.isMainLoad() && DataType.T99.equals(dataType)) {
                    DeviceHistory history = historyList.stream()
                                                       .filter(dh -> dh.getReportTime().equals(cursorEnd))
                                                       .findFirst()
                                                       .orElse(historyInPeriod.get(lastIndex));
                    MeasureData measureData = history.getMeasureData();
                    if (measureData != null
                            && measureData.getActivePower() != null
                            && measureData.getKWh() != null) {
                        electricData.setActivePower(measureData.getActivePower());
                        electricData.setTotalkWh(measureData.getKWh());
                    }
                }

                result.add(electricData);
            }

            cursor = cursorEnd;
        }

        return result;
    }


    private BigDecimal calculator15KW(String deviceId, Date start, Date end, LoadType loadType) {
        List<DeviceHistory> dataList = deviceService.findDeviceHistoryByDeviceIdAndTime(deviceId,
                DatetimeUtils.add(start, Calendar.SECOND, -catchBuffer),
                DatetimeUtils.add(end, Calendar.SECOND, catchBuffer));
        if (dataList.size() > 1) {
            DeviceHistory dhStart = dataList.get(0);
            DeviceHistory dhEnd = dataList.get(dataList.size() - 1);
            return calculator15KW(dhStart, dhEnd, loadType);
        } else {
            return null;
        }
    }

    private BigDecimal calculator15KW(DeviceHistory start, DeviceHistory end, LoadType loadType) {
        if (start != null && end != null) {
            BigDecimal totalValue = BigDecimal.ZERO;
            MeasureData measureDataStart = start.getMeasureData();
            MeasureData measureDataEnd = end.getMeasureData();
            switch (loadType) {
                case M3:
                    totalValue = (measureDataEnd.getDischargeKWh().subtract(measureDataStart.getDischargeKWh()))
                            .subtract((measureDataEnd.getChargeKWh().subtract(measureDataStart.getChargeKWh())));
                    break;
                default:
                    totalValue = measureDataEnd.getKWh().subtract(measureDataStart.getKWh());
                    log.debug(Constants.TIMESTAMP_FORMAT.format(end.getReportTime()) + " "
                            + Constants.TIMESTAMP_FORMAT.format(start.getReportTime()) + " "
                            + measureDataEnd.getKWh().toPlainString() + " " + measureDataStart.getKWh().toPlainString()
                            + " " + totalValue.toPlainString());
                    break;
            }

            // 外插法計算出該區段需量
            return Utility.secKWhTo15KW(totalValue, end.getReportTime().getTime() - start.getReportTime().getTime());
        } else {
            return null;
        }
    }

}