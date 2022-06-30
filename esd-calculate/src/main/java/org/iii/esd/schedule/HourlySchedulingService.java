package org.iii.esd.schedule;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.caculate.Utility;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.PolicyDevice;
import org.iii.esd.enums.PolicyService;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.vo.Level3;
import org.iii.esd.utils.DatetimeUtils;

/***
 * 每小時調度排程，對應HourlySchedulingService
 *
 * @author iii
 *
 */
@Log4j2
@Service
public class HourlySchedulingService {
    public static final int IDX0730 = 29;
    public static final int IDX0745 = 30;
    public static final int IDX1000 = 39;
    public static final int IDX1200 = 47;
    public static final int IDX1215 = 48;
    public static final int IDX1300 = 51;
    public static final int IDX1500 = 59;
    public static final int IDX1700 = 67;
    public static final int IDX1715 = 68;
    public static final int IDX1730 = 69;
    public static final int IDX2400 = 95;
    public static final int IDX_TOMORROW = 96;
    public static final int ENTITYLEN = 96;
    public static final int minIdx = 0;
    public static final int maxIdx = 1;
    public static final BigDecimal toKwh = new BigDecimal(0.25);
    public static final BigDecimal minus = new BigDecimal(-1);
    public boolean DEBUG_MODE = false;
    @Autowired
    ElectricDataRepository electricDataRepository;

    /***
     * 一般執行入口
     *
     * @param input
     * @throws Exception
     */
    public void SchedulingByStrategy(SchedulingInputModel input) throws Exception {
        // 取得全日預測資料

        List<ElectricData> forecastDatas = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(
                input.fieldId, DataType.getCode(input.FORECAST_DATA_TYPE), input.Schedule_Day_Start,
                input.Schedule_Day_End);

        // 取出實際資料，包含查詢前一日，不是查詢當天
        // 改成前一小時，不想抓太多資料

        List<ElectricData> historyDatas = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(input.fieldId,
                DataType.getCode(input.REAL_DATA_TYPE),
                Utility.FixTime(Utility.GetControlDay(input.Schedule_Day_Start), 0, -1, 0, 0, 0).getTime(),
                input.Schedule_Start);

        // 更新RMMD跟TRHD，先更新，之後要拆程測試不從DB會方便些?
        UpdateTRHD(input);

        // 更新電池蓄電量
        UpdateInitialSOC(input, historyDatas);

        // 執行調度排程

        List<ElectricData> scheduldDatas = SchedulingByStrategy(input, forecastDatas, historyDatas);

        // 更新調度排程資料
        Optional<ElectricData> minTime = scheduldDatas.stream().min(Comparator.comparing(ElectricData::getTime));
        Optional<ElectricData> maxTime = scheduldDatas.stream().max(Comparator.comparing(ElectricData::getTime));
        if (minTime.isPresent() && maxTime.isPresent()) {
            try {
                log.trace(
                        "FieldId:{}, dataType:{}, from:{}, until:{} \nTotalChargeM3:{},TotalDischargeM3:{},TotalM2:{}, LastMsoc:{}",
                        input.fieldId,
                        input.SCHEDULE_DATA_TYPE,
                        minTime.get().getTime(),
                        maxTime.get().getTime(),
                        scheduldDatas.stream().filter(a -> a.getM3kW().compareTo(BigDecimal.ZERO) <= 0).map(ElectricData::getM3kW)
                                     .mapToDouble(BigDecimal::doubleValue).sum(),
                        scheduldDatas.stream().filter(a -> a.getM3kW().compareTo(BigDecimal.ZERO) > 0).map(ElectricData::getM3kW)
                                     .mapToDouble(BigDecimal::doubleValue).sum(),
                        scheduldDatas.stream().map(ElectricData::getM2kW).mapToDouble(BigDecimal::doubleValue).sum(),
                        scheduldDatas.get(scheduldDatas.size() - 1).getMsoc()
                );
            } catch (Throwable ex) {

            }
            // 清除舊資料，重新新增排程資料
            electricDataRepository.delete(input.fieldId, DataType.getCode(input.SCHEDULE_DATA_TYPE),
                    minTime.get().getTime(), maxTime.get().getTime());
            electricDataRepository.insert(scheduldDatas);
        }
    }

    /***
     * 利用附載譽測資料以及歷史資料進行調度排程 不處理聚合調度，直接處理個別調度
     *
     * @param input
     * @param forecastDatas
     * @param historyDatas
     * @return
     * @throws Exception
     */
    public List<ElectricData> SchedulingByStrategy(SchedulingInputModel input, List<ElectricData> forecastDatas,
            List<ElectricData> historyDatas) throws Exception {
        log.trace("FieldId:{}, Start:{}, End:{}", input.fieldId, input.Schedule_Day_Start, input.Schedule_Day_End);
        // 判斷策略模式
        if (input.config.getDispatching().getMode() == 1) {
            return CustomScheduling(input, forecastDatas, historyDatas);
        } else if (input.config.getDispatching().getMode() == 2) {
            return CustomScheduling(input, forecastDatas, historyDatas);
        }
        return null;
    }

    /**
     * 檢查MSOC變化是否正常，只有DEBUG模式才用
     *
     * @param input
     * @param eds
     */
    public void CheckMsoc(SchedulingInputModel input, ElectricData[] eds) {
        if (!DEBUG_MODE) {
            return;
        }
        List<ElectricData> scheduleDatas = Arrays.stream(eds)
                                                 .filter(a -> a.getTime().compareTo(input.getSchedule_Start()) > 0)
                                                 .collect(Collectors.toList());
        for (int i = 1; i < scheduleDatas.size(); i++) {
            ElectricData a = scheduleDatas.get(i - 1);
            ElectricData b = scheduleDatas.get(i);
            int result = b.getM3kW().compareTo(BigDecimal.ZERO);

            if (result == 0) {
                // 放電度數為0的話，電池電量不該有變化
                if (a.getMsoc().compareTo(b.getMsoc()) != 0) {
                    throw new IiiException(
                            String.format("%s  Msoc abnormal at standby. Index:%s, Lmsoc:%f,Rmsoc:%f,currentM3kW:%f",
                                    input.getSchedule_Start(), i, a.getMsoc(), b.getMsoc(), b.getM3kW()));
                }
            } else if (result < 1) {
                // 電池為負的話，要看到電池電量增加
                if (a.getMsoc().compareTo(b.getMsoc()) > 0) {
                    throw new IiiException(
                            String.format("%s  Msoc abnormal at charge. Index:%s, Lmsoc:%f,Rmsoc:%f,currentM3kW:%f",
                                    input.getSchedule_Start(), i, a.getMsoc(), b.getMsoc(), b.getM3kW()));
                }
            } else {
                // 反之，要看到電池電量減少
                if (a.getMsoc().compareTo(b.getMsoc()) < 0) {
                    throw new IiiException(
                            String.format("%s  Msoc abnormal at discharge. Index:%s, Lmsoc:%f,Rmsoc:%f,currentM3kW:%f",
                                    input.getSchedule_Start(), i, a.getMsoc(), b.getMsoc(), b.getM3kW()));
                }
            }
        }
    }

    /***
     * 個別調度演算法
     *
     * @param input
     * @param forecastDatas
     * @param historyDatas
     * @return
     * @throws Exception
     */
    public List<ElectricData> CustomScheduling(SchedulingInputModel input, List<ElectricData> forecastDatas,
            List<ElectricData> historyDatas) throws Exception {

        if (forecastDatas.size() < ENTITYLEN) {
            throw new Exception("排程資料不足96筆");
        }
        // 排程資料初始化
        ElectricData[] scheduleDatas = InitEntity(input);

        // 將預測資料寫入排程資料
        InitEntitybyForecastData(scheduleDatas, forecastDatas, true);

        // 需量反應，跳過，可以參考 HourlySchedulingService: Line 411-446

        // 一、電池初始充電運算步驟
        InitialChargeProc(input, scheduleDatas);
        CheckMsoc(input, scheduleDatas);

        // 二、電池全日削峰放電運算步驟
        PeakClippingDischargeProc(input, scheduleDatas);
        CheckMsoc(input, scheduleDatas);
        // 三、電池抑低削峰放電運算步驟，需量反應略過
        // PeakClippingDischargeSuppressionProc(input, scheduleDatas);

        // 四、冰機抑低卸載運算步驟，需量反應略過
        // UnloadSuppressionProc(input, scheduleDatas);

        // 五、電池餘量放電運算步驟
        // 餘量放電跟全日銷峰邏輯目前衝突
        ResidualDischargeProc(input, scheduleDatas);
        CheckMsoc(input, scheduleDatas);
        // 調整小數位數
        PostPorcessing(input, scheduleDatas);
        CheckMsoc(input, scheduleDatas);

        return Arrays.stream(scheduleDatas).skip(input.Schedule_Start_Idx).collect(Collectors.toList());
    }

    ElectricData[] InitEntity(SchedulingInputModel input) {
        Calendar dayStart = Calendar.getInstance();
        dayStart.setTime(input.Schedule_Day_Start);

        ElectricData[] entity = new ElectricData[ENTITYLEN];
        for (int idx = 0; idx < ENTITYLEN; idx++) {
            dayStart.add(Calendar.MINUTE, 15);
            entity[idx] = new ElectricData();
            // entity[idx].setFieldProfile(fieldProfile);
            // setFieldProfile怪怪的
            entity[idx].setDataType(DataType.getCode(input.SCHEDULE_DATA_TYPE)); // 代表資料為排程結果類型
            entity[idx].setTime(dayStart.getTime());// 該15分鐘時段的結尾時間
            entity[idx].setM0kW(BigDecimal.ZERO);
            entity[idx].setM1kW(BigDecimal.ZERO);
            entity[idx].setM2kW(BigDecimal.ZERO);
            entity[idx].setM3kW(BigDecimal.ZERO);
            entity[idx].setM5kW(BigDecimal.ZERO);
            entity[idx].setM6kW(BigDecimal.ZERO);
            entity[idx].setM7kW(BigDecimal.ZERO);
            entity[idx].setM8kW(BigDecimal.ZERO);
            entity[idx].setM9kW(BigDecimal.ZERO);
            entity[idx].setM10kW(BigDecimal.ZERO);
            entity[idx].setMsoc(BigDecimal.ZERO);
            entity[idx].setMsocRsv(BigDecimal.ZERO);
            entity[idx].setTrhd(BigDecimal.ZERO);
        }
        return entity;
    }

    /***
     * 判斷L2策略是否執行
     *
     * @param input
     * @param device
     * @param service
     * @return
     */
    boolean IsL2ConfigActive(SchedulingInputModel input, PolicyDevice device, PolicyService service) {

        return Utility.IsL2ConfigActive(input.config, device, service);

    }

    /***
     * 判斷L3策略是否執行
     *
     * @param input
     * @param device
     * @param service
     * @return
     */
    Level3 IsL3ConfigActive(SchedulingInputModel input) {
        return Utility.IsL3ConfigActive(input.config);
    }

    /***
     * 小數位數調整
     *
     * @param input
     * @param scheduleDatas
     */
    private void PostPorcessing(SchedulingInputModel input, ElectricData[] scheduleDatas) {
        FieldProfile fieldProfile = new FieldProfile();
        fieldProfile.setId(input.fieldId);
        Arrays.stream(scheduleDatas).forEach(sd -> {
            sd.setFieldProfile(fieldProfile);
            sd.setScale();
            sd.balance();
        });
    }

    /***
     * 電池餘量放電運算步驟
     *
     * @param input
     * @param scheduleDatas
     */
    private void ResidualDischargeProc(SchedulingInputModel input, ElectricData[] scheduleDatas) {
        // TODO Auto-generated method stub
        Calc_MSOC_RSV(input, scheduleDatas);
        // 假日的話，不做餘量放電
        if (DatetimeUtils.isHoliday(input.Schedule_Day_Start)) {
            return;
        }
        if (DatetimeUtils.isHoliday(input.Schedule_Day_Start) || input.Max_Power.compareTo(BigDecimal.ZERO) <= 0
                || input.Discharge_Efficiency.compareTo(BigDecimal.ZERO) <= 0 || input.Schedule_Start_Idx >= IDX1715) { return; }
        if (!IsL2ConfigActive(input, PolicyDevice.ESS, PolicyService.C)) { return; }
        // 若為平日但L3調度策略[蓄電池][餘電釋放]設定值為1(平日停用/DR日啟用)，則跳過此運算步驟
        if (IsL3ConfigActive(input).getReleaseType() == 1 && !input.Is_Suppression_Day) {
            return;
        }
        Optional<ElectricData> ominSOC = Arrays.stream(scheduleDatas).skip(IDX1700)
                                               .min(Comparator.comparing(ElectricData::getMsoc));
        if (ominSOC.isPresent()) {
            BigDecimal minSOC = ominSOC.get().getMsoc();
            BigDecimal to_discharge_power = minSOC.multiply(input.Discharge_Efficiency).divide(toKwh, 3,
                    BigDecimal.ROUND_HALF_UP);
            // 依17:00往前至07:30且不早於[排程開頭時間]的時段順序，逐次將每個15分鐘時段的需量減去to_discharge_power與(電池最大放電功率–該時段的電池放電功率)中之較小值
            // 每次都將該時段的電池放電功率加上該值
            // 每次都將to_discharge_power減去該值，直到to_discharge_power等於0為止
            int idx = IDX1700;
            int period_start_idx = Math.max(input.Schedule_Start_Idx, IDX0745);
            for (; idx >= period_start_idx && to_discharge_power.compareTo(BigDecimal.ZERO) > 0; idx--) {
                BigDecimal dischargeable = to_discharge_power
                        .min(input.Max_Power.subtract(scheduleDatas[idx].getM3kW()));
                scheduleDatas[idx].setM3kW(scheduleDatas[idx].getM3kW().add(dischargeable));
                scheduleDatas[idx].balance();
                to_discharge_power = to_discharge_power.subtract(dischargeable);
            }
            // 從上一步驟結束時的時段開始直到當天最後一個時段，依序逐次將該時段的電池餘量設為 (前一時段的電池餘量–該時段的電池放電功率 × 0.25 h ÷
            // 電池放電效率)
            for (idx++; idx < ENTITYLEN; idx++) {
                BigDecimal msoc = ((idx == input.Schedule_Start_Idx) ? input.Initial_SOC
                        : scheduleDatas[idx - 1].getMsoc());
                BigDecimal tempmsoc = msoc.subtract(scheduleDatas[idx].getM3kW().multiply(toKwh)
                                                                      .divide(input.Discharge_Efficiency, 3, BigDecimal.ROUND_HALF_UP));
                /*
                 * 小數進位問題，不報錯，調整成正確數值
                 */
                if (tempmsoc.compareTo(BigDecimal.ZERO) < 0) {
                    log.info("msoc should be greater or equal to zero.");
                    // 小數進位問題，暫時沒好想法解決
                    scheduleDatas[idx].setM3kW(
                            msoc.multiply(input.Discharge_Efficiency).divide(toKwh, 3, BigDecimal.ROUND_HALF_UP));
                    tempmsoc = BigDecimal.ZERO;
                }
                scheduleDatas[idx].setMsoc(tempmsoc);
            }
        }

    }

    /***
     * 在削峰相關運算步驟都完成後，計算為了削峰而保留的電池餘量
     *
     * @param input
     * @param scheduleDatas
     */
    private void Calc_MSOC_RSV(SchedulingInputModel input, ElectricData[] scheduleDatas) {
        // TODO Auto-generated method stub
        int period_start_idx = Math.max(input.Schedule_Start_Idx, IDX0730);

        // 依時段順序由晚至早，逐次將「排程開頭時間後07:15~24:00」之期間內各時段的電池保留餘量設為(該時段的電池餘量-[24:00的電池餘量])
        for (int idx = IDX2400; idx >= period_start_idx; idx--) {
            scheduleDatas[idx].setMsocRsv(scheduleDatas[idx].getMsoc().subtract(scheduleDatas[IDX2400].getMsoc()));
        }

        // 將「排程開頭時間後00:00~07:15」之期間內各時段的電池保留餘量設為[07:30的電池保留餘量]
        for (int idx = Math.max(input.Schedule_Start_Idx, 0); idx < IDX0730; idx++) {
            scheduleDatas[idx].setMsocRsv(scheduleDatas[IDX0730].getMsocRsv());
        }
    }

    /***
     * 電池全日削峰放電運算步驟
     *
     * @param input
     * @param scheduleDatas
     * @throws Exception
     */
    private void PeakClippingDischargeProc(SchedulingInputModel input, ElectricData[] scheduleDatas) throws Exception {
        // TODO Auto-generated method stub
        // 電池最大功率為0 ，無法充電
        if (input.Max_Power.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 電池放電效率為0
        if (input.Discharge_Efficiency.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        boolean isConfigActive = IsL2ConfigActive(input, PolicyDevice.ESS, PolicyService.A);
        if (!isConfigActive) {
            return;
        }
        // 取「排程開頭時間後07:30~17:00」之[期間]內所有15分鐘時段的需量之最大值來當作電池可削平峰值的上限
        BigDecimal ceiling = BigDecimal.ZERO;
        int period_start_idx = Math.max(input.Schedule_Start_Idx, 0);
        // 改為尋找全天最高負載 by KFN
        for (int idx = period_start_idx; idx < IDX_TOMORROW; idx++) {
            ceiling = ceiling.max(scheduleDatas[idx].getM1kW());
        }
        BigDecimal floor = input.TRHD.max(ceiling.subtract(input.Max_Power));

        // 找出電池最多可削平峰值
        BigDecimal targetPeak = null;
        BigDecimal step = SchedulingInputModel.Step;
        // 計算[期間]內各時段需量超出peak的差值並加總，直到差值總和不超出([07:30以後的排程開頭電池餘量] × 電池放電效率 ÷ 0.25h)為止
        for (BigDecimal peak = floor; peak.compareTo(ceiling) <= 0; peak = peak.add(step)) {
            // log.trace("FieldId:{}, currentpeak：{}", input.fieldId, peak);
            BigDecimal currentMSOC = input.Initial_SOC;
            // 兩階段處理，充電前跟充電後
            try {
                for (int idx = period_start_idx; idx < IDX0730; idx++) {

                    BigDecimal demand = scheduleDatas[idx].getM1kW().subtract(peak);
                    if (demand.compareTo(BigDecimal.ZERO) >= 0) {
                        BigDecimal dischargeable = demand.min(input.Max_Power);
                        currentMSOC = MSOC_Change(input, currentMSOC, dischargeable).msoc;
                    } else {
                        BigDecimal cdemand = input.Max_Power.min(input.TRHD.subtract(scheduleDatas[idx].getM1kW()));
                        BigDecimal chargeable = cdemand.max(BigDecimal.ZERO);
                        currentMSOC = MSOC_Change(input, currentMSOC, chargeable.multiply(minus)).msoc;
                    }

                }
                for (int idx = period_start_idx > IDX0730 ? period_start_idx : IDX0730; idx < IDX_TOMORROW; idx++) {
                    if (scheduleDatas[idx] != null) {
                        BigDecimal demand = scheduleDatas[idx].getM1kW().subtract(peak);
                        if (demand.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal dischargeable = demand.min(input.Max_Power);
                            currentMSOC = MSOC_Change(input, currentMSOC, dischargeable).msoc;
                        }
                    }
                }
                targetPeak = peak;
                break;
            } catch (Throwable ex) {
                // log.error(ex.getMessage(), ex);
            }
        }
        log.info("FieldId:{}, ceiling：{}, floor:{}, targetPeak：{}", input.fieldId, ceiling, floor, targetPeak);
        /**
         * 全部都要做，不然電池度數無法正確更新
         */
        {

            // 依時段順序由早至晚，逐次計算[期間]內各時段需量超出peak的差值－excess
            // 每次都將該時段的需量減去excess
            // 每次都將該時段的電池放電功率設為excess
            // 每次都將該時段的電池餘量設為(前一時段的電池餘量 –excess × 0.25 h ÷ 電池放電效率)
            for (int idx = period_start_idx; idx < IDX0730; idx++) {
                EssAction action = MSOC_Change(input,
                        ((idx == input.Schedule_Start_Idx) ? input.Initial_SOC : scheduleDatas[idx - 1].getMsoc()),
                        BigDecimal.ZERO);
                if (targetPeak != null) {
                    if (scheduleDatas[idx].getM1kW().compareTo(targetPeak) > 0) {
                        BigDecimal dischargeable = input.Max_Power
                                .min(scheduleDatas[idx].getM1kW().subtract(targetPeak));
                        action = MSOC_Change(input, ((idx == input.Schedule_Start_Idx) ? input.Initial_SOC
                                : scheduleDatas[idx - 1].getMsoc()), dischargeable);
                    } else {
                        BigDecimal cdemand = input.Max_Power.min(input.TRHD.subtract(scheduleDatas[idx].getM1kW()));
                        BigDecimal chargeable = cdemand.max(BigDecimal.ZERO);
                        action = MSOC_Change(input, ((idx == input.Schedule_Start_Idx) ? input.Initial_SOC
                                : scheduleDatas[idx - 1].getMsoc()), chargeable.multiply(minus));
                    }
                }
                scheduleDatas[idx].setM3kW(action.m3Kw);
                scheduleDatas[idx].setMsoc(action.msoc);
                scheduleDatas[idx].setPks(targetPeak);
                scheduleDatas[idx].balance();
            }
            // 上下兩段應該是可以合併，當時因為時間因素，快速改了就沒特別再去細想
            try {
                for (int idx = period_start_idx > IDX0730 ? period_start_idx : IDX0730; idx < IDX_TOMORROW; idx++) {
                    // 每小時排程的時候，前面資料可能是空的
                    if (scheduleDatas[idx] != null) {
                        EssAction action = MSOC_Change(input, ((idx == input.Schedule_Start_Idx) ? input.Initial_SOC
                                : scheduleDatas[idx - 1].getMsoc()), BigDecimal.ZERO);
                        if (targetPeak != null) {
                            if (scheduleDatas[idx].getM1kW().compareTo(targetPeak) > 0) {
                                BigDecimal dischargeable = input.Max_Power
                                        .min(scheduleDatas[idx].getM1kW().subtract(targetPeak));
                                action = MSOC_Change(input, ((idx == input.Schedule_Start_Idx) ? input.Initial_SOC
                                        : scheduleDatas[idx - 1].getMsoc()), dischargeable);
                            }
                        }
                        scheduleDatas[idx].setM3kW(action.m3Kw);
                        scheduleDatas[idx].setMsoc(action.msoc);
                        scheduleDatas[idx].setPks(targetPeak);
                        scheduleDatas[idx].balance();
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private EssAction MSOC_Change(SchedulingInputModel input, BigDecimal currentMSOC, BigDecimal m3KW)
            throws Exception {
        // TODO Auto-generated method stub
        EssAction result = new EssAction();
        result.m3Kw = (BigDecimal.ZERO);
        result.msoc = (currentMSOC);
        if (m3KW.compareTo(BigDecimal.ZERO) > 0) {
            m3KW = m3KW.min(input.Max_Power);
            BigDecimal usesoc = m3KW.multiply(toKwh).divide(input.Discharge_Efficiency, 3, BigDecimal.ROUND_HALF_UP);

            if (currentMSOC.compareTo(usesoc) < 0) {
                throw new IiiException("電池蓄電量不足");
            }
            currentMSOC = currentMSOC.subtract(usesoc);
            result.m3Kw = (m3KW);
            result.msoc = (currentMSOC);

        } else if (m3KW.compareTo(BigDecimal.ZERO) < 0) {
            m3KW = m3KW.max(input.Max_Power.multiply(minus));
            BigDecimal savesoc = m3KW.multiply(minus).multiply(toKwh).multiply(input.Charge_Efficiency);
            // 以前執行的時後沒有假設電池有放電深度，導致資料面，電池容量跟蓄電量不符
            // 發生排程蓄電量大於電池容量的狀況，導致推算M3KW為正數，
            if (currentMSOC.compareTo(input.Battery_Capacity) > 0) {
                savesoc = BigDecimal.ZERO;
                m3KW = BigDecimal.ZERO;
            } else if ((currentMSOC.add(savesoc)).compareTo(input.Battery_Capacity) > 0) {
                savesoc = input.Battery_Capacity.subtract(currentMSOC);
                if (savesoc.compareTo(BigDecimal.ZERO) < 0) {
                    savesoc = BigDecimal.ZERO;
                    // 防止BUG
                }
                m3KW = savesoc.multiply(minus).divide(toKwh, 3, BigDecimal.ROUND_HALF_UP)
                              .divide(input.Charge_Efficiency, 3, BigDecimal.ROUND_HALF_UP);
            }
            currentMSOC = currentMSOC.add(savesoc);
            result.m3Kw = (m3KW);
            result.msoc = (currentMSOC);
        }
        if (result.msoc.compareTo(BigDecimal.ZERO) < 0) {
            log.error("msoc should be greater or equal to zero.");
        }
        return result;
    }

    /***
     * 電池初始充電運算步驟
     *
     * @param input
     * @param scheduleDatas
     * @param battery_Capacity
     */
    private void InitialChargeProc(SchedulingInputModel input, ElectricData[] scheduleDatas) {
        // 最大蓄電量為0 ，無法充電
        if (input.Battery_Capacity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 電池最大功率為0 ，無法充電
        if (input.Max_Power.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 電池充電效率為0
        if (input.Charge_Efficiency.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 排程時段在07:30後，跳過充電流程
        if (input.Schedule_Start_Idx >= IDX0745) {
            return;
        }

        BigDecimal targetCapacity = input.Battery_Capacity;

        // 剩餘15分鐘時段待充電的功率總和 = (電池蓄電容量–排程開頭的電池餘量)÷電池充電效率÷ 0.25 h
        BigDecimal to_charge_power = (targetCapacity.subtract(input.Initial_SOC))
                .divide(input.Charge_Efficiency, 3, BigDecimal.ROUND_HALF_UP)
                .divide(toKwh, 3, BigDecimal.ROUND_HALF_UP);

        // 依[排程開頭時間]~07:30的順序，逐次將每個15分鐘時段的需量加上{to_charge, 電池最大充電功率, (TRHD–需量)}中不小於0之較小值
        // 每次都將該時段的電池放電功率設為(-1 × 該值)
        // 每次都將該時段的電池餘量設為(前一時段的電池餘量 + 該值 × 0.25 h × 電池充電效率)
        // 每次都將該值從to_charge_power中扣除
        for (int idx = input.Schedule_Start_Idx; idx < IDX0745; idx++) {
            BigDecimal demandtarget = input.TRHD.subtract(scheduleDatas[idx].getM1kW()); // TRHD–需量
            BigDecimal sectionMaxPower = to_charge_power.min(input.Max_Power); // to_charge, 電池最大充電功率
            BigDecimal chargeable = demandtarget.min(sectionMaxPower).max(BigDecimal.ZERO);

            scheduleDatas[idx].setM3kW(chargeable.multiply(minus));
            scheduleDatas[idx].balance();
            BigDecimal initSOC = (idx == input.Schedule_Start_Idx) ? input.Initial_SOC
                    : scheduleDatas[idx - 1].getMsoc();
            BigDecimal chargeKwh = chargeable.multiply(toKwh).multiply(input.Charge_Efficiency);
            scheduleDatas[idx].setMsoc(initSOC.add(chargeKwh));
            to_charge_power = to_charge_power.subtract(chargeable);
        }
        // 填入各時段電池度數正確數執
        for (int idx = IDX0745; idx <= IDX2400; idx++) {
            scheduleDatas[idx].setMsoc(scheduleDatas[idx - 1].getMsoc());
        }

    }

    /***
     * 更新RMMD跟TRHD
     *
     * @param input
     */
    @SuppressWarnings("deprecation")
    private void UpdateTRHD(SchedulingInputModel input) {
        List<ElectricData> historyData = electricDataRepository.findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(
                input.fieldId, DataType.getCode(input.REAL_DATA_TYPE), input.Schedule_Month_Start, input.Schedule_Start,
                new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "m1kW")));
        Optional<ElectricData> maxM1Data = historyData.stream()
                                                      .sorted(Comparator.comparing(ElectricData::getM1kW).reversed()).findFirst();
        if (maxM1Data.isPresent()) {
            input.RMMD = maxM1Data.get().getM1kW();
        }
        input.TRHD = input.TYOD.max(input.RMMD);
        log.trace("FieldId:{}, TRHD:{}, TYOD:{}, RMMD:{}", input.fieldId, input.TRHD, input.TYOD, input.RMMD);
    }

    /***
     * 將負載預測資料填入排程資料中<br/>
     * 將「排程開頭~當日結尾」之期間內所有15分鐘時段的需量加上各別時段的(電池放電功率 + 冰機卸載功率)<br/>
     * 各別時段的冰機用電功率加上該時段的冰機卸載功率<br/>
     * 各別時段的電池放電功率、電池餘量以及冰機卸載功率設為0
     *
     * @param entity
     * @param forecast_data
     * @param resetM6
     */
    private void InitEntitybyForecastData(ElectricData[] entity, List<ElectricData> forecast_data, boolean resetM6) {
        Arrays.stream(entity).forEach(ed -> {
            Optional<ElectricData> ofd = forecast_data.stream().filter(a -> a.getTime().equals(ed.getTime()))
                                                      .findFirst();
            if (ofd.isPresent()) {
                ElectricData fd = ofd.get();
                ed.setM2kW(fd.getM2kW());
                ed.setM5kW(fd.getM5kW());
                // 主要是給前置排程，要不要重置可控設備的出力
                if (resetM6) {
                    ed.setM6kW(fd.getM0kW().subtract(fd.getM5kW()));
                    ed.setM7kW(BigDecimal.ZERO);
                } else {
                    ed.setM6kW(fd.getM6kW());
                    ed.setM7kW(fd.getM7kW());
                }
                ed.balance();
            }
        });

    }

    /***
     * 更新最新電池蓄電量
     *
     * @param input
     * @param historyDatas
     */
    private void UpdateInitialSOC(SchedulingInputModel input, List<ElectricData> historyDatas) {
        if (historyDatas.size() > 0 && input.Discharge_Efficiency.compareTo(BigDecimal.ZERO) > 0) {
            ElectricData last = historyDatas.get(historyDatas.size() - 1);
            Date lastRealDataEnd = last.getTime();
            // 如果排程時間早於07:30，先把排程資料取出，然後計算可能的充電量，以此得到可能的蓄電量
            List<ElectricData> scheduleDatas = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(
                    input.fieldId, DataType.getCode(input.SCHEDULE_DATA_TYPE), lastRealDataEnd, input.Schedule_Start);
            input.UpdateInitialSOC(historyDatas, scheduleDatas);
            log.trace("FieldId:{}, MSOC:{}", input.fieldId, input.Initial_SOC);
        }

    }

}
