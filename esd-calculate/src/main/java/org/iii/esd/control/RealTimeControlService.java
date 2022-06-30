package org.iii.esd.control;

import java.math.BigDecimal;
import java.util.Calendar;
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
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.utils.DatetimeUtils;

/***
 * 即時控制，對應IndiviualRealTimeControlService.cs
 *
 * @author iii
 *
 */
@Log4j2
@Service
public class RealTimeControlService {
    @Autowired
    private ElectricDataRepository electricDataRepository;
    @Autowired
    private FieldProfileService fieldProfileService;

    /***
     * IndiviualRealTimeControlService::MainControl
     *
     * @param input
     * @return
     */
    public RecommendControl MainControl(RealTimeControlInputModel input) {
        RecommendControl result = new RecommendControl();
        // 若為週六但L1調度策略[調度日]設定值為1(非例假日(不含週六))，則跳過此運算步驟
        if (input.config.getDispatching().getDayType() == 1
                && Utility.GetControlDay(input.Control_Start).get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            return result;
        }

        ResetRealTimeControlInputModel(input);

        // (1)電池充電即時控制運算步驟
        ChargeControl(input, result);

        // (2)電池放電即時控制運算步驟
        DischargeControl(input, result);

        // (3)電池餘量放電即時控制運算步驟
        ResidualDischargeControl(input, result);

        // 4、5是需量反應，跳過不做

        // (4)電池抑低放電即時控制運算步驟
        // SuppressionDischargeControl(input, ref Control_M3_KW);

        // (5)冰機抑低卸載即時控制運算步驟
        // SuppressionUnloadControl(input, ref Control_M3_KW, ref Control_M7_KW);

        result.tdmd = input.TDMD;
        result.thdc = input.THDC;

        log.info("即時控制開頭時間：{}\nFieldId:{}\nTDMD:{}\nTHDC:{}\nM3:{}\nM7:{}", input.Control_Start, input.fieldId,
                input.TDMD, input.THDC, result.m3kW, result.m7kW);
        return result;
    }

    /***
     * 準備計算所需資料
     */
    void ResetRealTimeControlInputModel(RealTimeControlInputModel input) {
        input.CheckParameter();
        Optional<FieldProfile> oField = fieldProfileService.find(input.fieldId);
        if (!oField.isPresent()) {
            throw new IiiException("查無場域資訊");
        }
        input.field = oField.get();

        // 執行有其順序性
        LoadScheduleData(input);
        getNewestTDMDAndSocRsv(input);
        getNewestRTIP(input);
        getNewestRMMD(input);
        caculateWholeSectionAmount(input);

        // 計算當下~當下時段結束的分鐘數 = 15–當下分鐘 % 15
        // 還沒想好這段要怎麼改

        DecideTHDC(input);
        DecideControlTarget(input);
        CaculateTotalDemandOf3MinDatas(input);
        CaculateSOCClipUsable(input);
        // 不作需量，所以跳過
        // SearchInDemandResponseSection(input);

        log.info(
                "即時控制開頭時間：{}\nFieldId:{}\nDemand_Whole:{}\nControl_Whole:{}\nTHDC:{}\nRTIP:{}\nRMMD:{}\nSOC_usable:{}(SOC-RSV: {}-{})\nWhole_3Min:{}\nQuarter_Distance:{}",
                input.Control_Start, input.fieldId, input.Demand_Whole, input.Control_Whole, input.THDC, input.RTIP,
                input.RMMD, input.SOC_usable, input.SOC, input.SOC_RSV, input.Whole_3Min, input.Quarter_Distance);

    }

    @SuppressWarnings("deprecation")
    private void ResidualDischargeControl(RealTimeControlInputModel input, RecommendControl result) {
        // 從L2調度策略取得[蓄電池][尖離峰負載移轉]的設定值
        boolean isActive = Utility.IsL2ConfigActive(input.config, PolicyDevice.ESS, PolicyService.C);
        // 若[蓄電池][尖離峰負載移轉]設定不為啟用，則跳過此運算步驟
        if (!isActive) {
            return;
        }
        // 若為平日但L3調度策略[蓄電池][餘電釋放]設定值為1(平日停用/DR日啟用)，則跳過此運算步驟
        if (Utility.IsL3ConfigActive(input.config).getReleaseType() == 1 && !input.Is_Suppression_Day) {
            return;
        }
        // 若當日為[離峰日、週日]、當下時間在07:30之前或17:00以後、電池最大充／放電功率或電池放電效率不大於0，則跳過此運算步驟
        if (DatetimeUtils.isHoliday(input.Control_Day_Start) || input.Control_Start.compareTo(input.TODAY_0730) < 0
                || input.Control_Start.compareTo(input.TODAY_1700) >= 0
                || input.Max_Power.compareTo(BigDecimal.ZERO) <= 0
                || input.Discharge_Efficiency.compareTo(BigDecimal.ZERO) <= 0) { return; }

        // 計算當下3分鐘時段結尾時間超出全力放電臨界點的分鐘數 = SOC × 電池放電效率 ÷ 電池最大放電功率 × 60–( 17 × 60–當下小時 ×
        // 60–當下分鐘–3 )
        // 先算出要幾分鐘能夠放完，再看目前還剩下幾分鐘
        // 改成秒，這段可能有錯，要特別注意
        // 這段可能有錯，要特別注意
        BigDecimal critical_excess = input.SOC.multiply(input.Discharge_Efficiency).multiply(BigDecimal.valueOf(3600))
                                              .divide(input.Max_Power, 5, BigDecimal.ROUND_HALF_UP);

        BigDecimal remainSeconds = BigDecimal.valueOf((17 * 3600 - input.Control_Start.getHours() * 3600
                - input.Control_Start.getMinutes() * 60 - input.Control_Start.getSeconds() - input.timeInterval));
        critical_excess = critical_excess.subtract(remainSeconds);
        // 將[控制電池放電功率]設為Max( [控制電池放電功率], 電池最大放電功率 × Min( 1, critical_excess ÷ 3 ) )
        BigDecimal Control_M3_KW = result.getM3kW().max(input.Max_Power.multiply(BigDecimal.ONE
                .min(critical_excess.divide(BigDecimal.valueOf(input.timeInterval), 3, BigDecimal.ROUND_HALF_UP))));
        result.setM3kW(Control_M3_KW);

    }

    /***
     * 平常日/DR日的電池充電(墊基)放電(削峰/降載)即時控制
     *
     * @param input
     * @param result
     */
    private void DischargeControl(RealTimeControlInputModel input, RecommendControl result) {
        // 需量反應無視
        // 啟動需量競價相關計算為false則跳過墊基即時控制運算步驟
        /*
         * if (!input.Is_Demand_Bidding_Enabled) return;
         */
        // 若當下時間在07:30之前或17:00以後、電池最大充／放電功率或電池充/放電效率不大於0，則跳過此運算步驟
        // 時間部分控管有些異常，所以取消
        if (input.Max_Power.compareTo(BigDecimal.ZERO) <= 0 || input.Charge_Efficiency.compareTo(BigDecimal.ZERO) <= 0
                || input.Discharge_Efficiency.compareTo(BigDecimal.ZERO) <= 0) { return; }
        // 若調度策略[蓄電池-削峰]並非啟用狀態，則跳過此運算步驟
        if (!Utility.IsL2ConfigActive(input.config, PolicyDevice.ESS, PolicyService.A)) { return; }
        // 計算剩餘3分鐘時段可放電的功率總和 = SOC_usable × 電池放電效率 ÷ 0.05 h
        BigDecimal dischargeable = input.SOC_usable.multiply(input.Discharge_Efficiency)
                                                   .divide(input.GetHoursInterval(), 3, BigDecimal.ROUND_HALF_UP);
        // 若當下非需量競價抑低期間、當下時段的排程電池保留餘量與SOC皆>0、且dischargeable < Min( 電池最大放電功率,
        // demand_whole–control_whole )，則執行「電池保留餘量調用步驟」
        BigDecimal demand = input.Max_Power.min(input.Demand_Whole.subtract(input.Control_Whole));
        // input.Is_Suppression_Period 放掉需量，所以下麵辣段部會進去
        log.trace("Is_Suppression_Period:{}, SOC_RSV:{}, SOC:{}, dischargeable:{}, demand:{} ",
                input.Is_Suppression_Period, input.SOC_RSV, input.SOC, dischargeable, demand);
        if (!input.Is_Suppression_Period && input.SOC_RSV.compareTo(BigDecimal.ZERO) > 0
                && input.SOC.compareTo(BigDecimal.ZERO) > 0 && dischargeable.compareTo(demand) < 0) { SOC_RSV_Invoke(input, result); }

        // 將[控制電池放電功率]設為Max( 0, Min( dischargeable, 電池最大放電功率,
        // demand_whole–control_whole) )
        BigDecimal Control_M3_KW = input.Max_Power.min(input.Demand_Whole.subtract(input.Control_Whole))
                                                  .min(dischargeable).max(BigDecimal.ZERO);
        result.setM3kW(Control_M3_KW);
    }

    /***
     * 電池保留餘量調用步驟
     *
     * @param realTimeControl
     * @param result
     */
    @SuppressWarnings("deprecation")
    private void SOC_RSV_Invoke(RealTimeControlInputModel realTimeControl, RecommendControl result) {
        // RealTimeControlService
        // private static void SOC_RSV_Invoke(RealTimeControlInputModel realTimeControl,
        // ref double dischargeable, ref bool Is_SOC_RSV_Invoked)
        ElectricData[] entity = new ElectricData[96];

        // 當下15分鐘時段序號
        int control_quarter_idx = realTimeControl.Control_Quarter_Start.getHours() * 4
                + realTimeControl.Control_Quarter_Start.getMinutes() / 15;
        List<ElectricData> schedule_data_target = realTimeControl.Schedule_Data.stream()
                                                                               .filter(a -> a.getTime().compareTo(
                                                                                       realTimeControl.Control_Quarter_Start) > 0)
                                                                               .collect(Collectors.toList());
        // log.debug("當下時段開頭~當日結束的排程資料筆數：" + schedule_data_target.size());
        if (schedule_data_target.size() < (96 - control_quarter_idx)) { throw new IiiException("排程結果資料不完整"); }

        for (ElectricData i : schedule_data_target) {
            int idx = (i.getTime().getHours() * 4 + i.getTime().getMinutes() / 15 + 95) % 96; // DATE_TIME：00:00為最後一個時段，00:15為第一個時段
            entity[idx] = i;
        }
        /* 計算電池放電功率的需要量與可用量 */
        // 計算當下3分鐘時段需要的15分鐘時段放電功率 = Min( 電池最大放電功率, demand_whole–control_whole ) × 0.05 h
        // ÷ 0.25 h
        // 計算當下3分鐘時段需要的15分鐘時段放電功率 = Min( 電池最大放電功率, demand_whole–control_whole ) × 0.05 h
        // X 4 h

        BigDecimal require = realTimeControl.Max_Power
                .min(realTimeControl.Demand_Whole.subtract(realTimeControl.Control_Whole)).multiply(Utility.kwhTokW)
                .multiply(realTimeControl.GetHoursInterval());
        // 計算當下電池可用的15分鐘時段放電功率總值 = SOC × 電池放電效率 ÷ 0.25 h
        // 計算當下電池可用的15分鐘時段放電功率總值 = SOC × 電池放電效率 X 4 h
        BigDecimal supply = realTimeControl.SOC.multiply(realTimeControl.Discharge_Efficiency)
                                               .multiply(Utility.kwhTokW);

        /* 重新削峰放電運算 */
        // 計算電池未放電時的預估整體需量之平均值 = demand_whole ÷ whole_3min
        BigDecimal esti_demand = realTimeControl.Demand_Whole.divide(BigDecimal.valueOf(realTimeControl.Whole_3Min), 3,
                BigDecimal.ROUND_HALF_UP);
        // 計算整體控制3分鐘時段佔15分鐘時段的時間比例 = whole_3min ÷ 5
        // whole_3min是目前時段總共累積資料數量
        // sectionCount是目前時段最終所有資料數量，求比例就是這兩者相除
        // 原本寫whole_3min ÷ 5 是因為whole_3min原本只考慮3分鐘case所以執行時，就是1~5
        BigDecimal sectionCount = BigDecimal.valueOf(Utility.MINUTES_15)
                                            .divide(BigDecimal.valueOf(realTimeControl.timeInterval), 3, BigDecimal.ROUND_HALF_UP);
        BigDecimal whole_ratio = BigDecimal.valueOf(realTimeControl.Whole_3Min).divide(sectionCount, 3,
                BigDecimal.ROUND_HALF_UP);
        // 將[當下時段結尾~當日結束]之[期間]內所有時段的目標電池放電功率設為0
        BigDecimal[] target_discharge = new BigDecimal[96];
        // 將[期間]內所有時段之目標需量設為各別時段的(排程需量 + 排程電池放電功率 + 排程冰機卸載功率)
        // 設定電池可削平峰值的上限 = [期間]內所有時段的目標需量之最大值
        BigDecimal[] target_demand = new BigDecimal[96];

        BigDecimal ceiling = BigDecimal.ZERO;
        for (int i = control_quarter_idx + 1; i < 96; i++) {
            target_demand[i] = entity[i].getM1kW().add(entity[i].getM3kW()).add(entity[i].getM7kW());
            ceiling = ceiling.max(target_demand[i]);
        }
        // 設定電池可削平峰值的下限 = THDC
        BigDecimal floor = realTimeControl.THDC;
        // 將電池可削平峰值當作變數
        BigDecimal peak;
        // 將peak的值從floor往ceiling逐次遞增0.001 kW
        // 每次peak遞增後皆計算[期間]內各時段之目標需量超出peak的差值並加總得到::total_excess
        // 若total_excess + Min ( require, Max ( 0, esti_demand–peak ) × whole_ratio ) ≦
        // supply，則peak即為[期間]內的可削平峰值
        BigDecimal step = BigDecimal.valueOf(0.001);
        for (peak = floor; peak.compareTo(ceiling) < 0; peak = peak.add(step)) {
            BigDecimal total_excess = BigDecimal.ZERO;
            for (int idx = control_quarter_idx + 1; idx < 96; idx++) {
                total_excess = total_excess.add(BigDecimal.ZERO.max(target_demand[idx].subtract(peak)));
            }

            BigDecimal checkSum = total_excess
                    .add(require.min(BigDecimal.ZERO.max(esti_demand.subtract(peak))).multiply(whole_ratio));
            if (checkSum.compareTo(supply) <= 0) { break; }
        }
        peak = peak.setScale(3, BigDecimal.ROUND_HALF_UP);

        /* 更新電池相關餘量以及剩餘3分鐘時段可放電的功率總和 */
        // 將THDC與TDMD重設為peak
        realTimeControl.THDC = realTimeControl.TDMD = peak;
        // 重新計算電池放電即時控制的整體目標值 = THDC * whole_3min
        realTimeControl.Control_Whole = realTimeControl.THDC.multiply(BigDecimal.valueOf(realTimeControl.Whole_3Min));

        /*
         * // 將[當下時段開始~當日結束]內所有時段的排程TDMD設為peak // TDMD沒有需量的用途很怪，也沒啥參考的地方，出現的地方都在需量反應內
         * for (int idx = control_quarter_idx; idx < 96; idx++) entity[idx].TDMD = peak;
         */
        // 將[期間]內各時段的目標電池放電功率設為Max( 0, 該時段的目標需量–peak )
        for (int idx = control_quarter_idx + 1; idx < 96; idx++) {
            target_discharge[idx] = BigDecimal.ZERO.max(target_demand[idx].subtract(peak));
        }

        // 依時段順序由晚至早，逐次將[期間]內各別時段的前一時段之排程電池保留餘量設為(該時段的目標電池放電功率 × 0.25 h ÷ 電池放電效率 +
        // 該時段的排程電池保留餘量)，再將前一時段的排程電池回填餘量設為Max(0, 前一時段的電池餘量–前一時段的電池保留餘量)
        // DB更新排程電池保留餘量
        try {
            // ESDTMongoDB.ElectricDatas.DeleteMany(a => a.Profile_ID ==
            // realTimeControl.Profile_ID && a.DATA_TYPE ==
            // realTimeControl.SCHEDULE_DATA_TYPE && a.DATE_TIME >
            // realTimeControl.Control_Quarter_Start && a.DATE_TIME <=
            // realTimeControl.TODAY_2400);
            for (int idx = 95; idx > control_quarter_idx; idx--) {
                if (entity[idx - 1] != null) {
                    BigDecimal msocRsv = target_discharge[idx].multiply(Utility.toKwh)
                                                              .divide(realTimeControl.Discharge_Efficiency, 3, BigDecimal.ROUND_HALF_UP)
                                                              .add(entity[idx].getMsocRsv()).setScale(3, BigDecimal.ROUND_HALF_UP);
                    entity[idx - 1].setMsocRsv(msocRsv);

                    BigDecimal msocBackfill = BigDecimal.ZERO
                            .max(entity[idx - 1].getMsoc().subtract(entity[idx - 1].getMsocRsv()))
                            .setScale(3, BigDecimal.ROUND_HALF_UP);
                    entity[idx - 1].setMsocBackfill(msocBackfill);
                    electricDataRepository.save(entity[idx - 1]);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        result.setM3kW(realTimeControl.SOC_usable.multiply(realTimeControl.Discharge_Efficiency)
                                                 .divide(realTimeControl.GetHoursInterval(), 3, BigDecimal.ROUND_HALF_UP));
        result.setSocRsvInvoked(true);

    }

    /***
     * 電池充電即時控制：若當下為凌晨離峰期間，將電池充飽電
     *
     * @param input
     * @param result
     */
    private void ChargeControl(RealTimeControlInputModel input, RecommendControl result) {
        // 若當下時間在07:30以後、電池蓄電容量、電池最大充／放電功率或電池充電效率不大於0，則跳過此運算步驟
        if (input.Control_Start.compareTo(input.TODAY_0730) >= 0
                || input.Battery_Capacity.compareTo(BigDecimal.ZERO) <= 0
                || input.Max_Power.compareTo(BigDecimal.ZERO) <= 0
                || input.Charge_Efficiency.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 計算剩餘3分鐘時段待充電的功率總和 = (電池蓄電容量–SOC ) ÷ 電池充電效率 ÷ 0.05 h
        // 0.05h就是 3分鐘 / 60分鐘 = 1/20 = 0.05H
        // 未來要改成秒，所以改成input.timeInterval / 3600 秒
        BigDecimal to_charge = (input.Battery_Capacity.subtract(input.SOC))
                .multiply(BigDecimal.valueOf(input.timeInterval))
                .divide(input.Charge_Efficiency, 3, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(3600), 3, BigDecimal.ROUND_HALF_UP);
        // 將[控制電池放電功率]設為( -1 × Max( 0, Min( to_charge, 電池最大充電功率,
        // control_whole–demand_whole ) ) )
        BigDecimal Control_M3_KW = BigDecimal.ZERO
                .max(to_charge.min(input.Max_Power.min(input.Control_Whole.subtract(input.Demand_Whole))));
        result.setM3kW(Control_M3_KW.multiply(Utility.minus));
    }

    /***
     * 計算電池削峰可用餘量 = Min( SOC_usable, ( 前一時段的排程電池保留餘量–當下時段的排程電池保留餘量 ) ×
     * Quarter_Distance ÷ 15 )
     *
     * @param input
     */
    private void CaculateSOCClipUsable(RealTimeControlInputModel input) {
        Optional<ElectricData> schedule_data_previous = input.Schedule_Data.stream()
                                                                           .filter(a -> a.getTime().equals(input.Control_Quarter_Start))
                                                                           .findFirst();
        if (schedule_data_previous.isPresent()) {
            input.SOC_RSV_prev = schedule_data_previous.get().getMsocRsv();
            input.SOC_clip_usable = input.SOC_usable.min(
                    (input.SOC_RSV_prev.subtract(input.SOC_RSV)).multiply(BigDecimal.valueOf(input.Quarter_Distance))
                                                                .divide(BigDecimal.valueOf(Utility.MINUTES_15)));
            /*
             * 改成秒 this.SOC_RSV_prev = schedule_data_previous.First().MSOC_RSV;
             * this.SOC_clip_usable = Math.Min(this.SOC_usable, (this.SOC_RSV_prev -
             * this.SOC_RSV) * this.Quarter_Distance / 15.0);
             **/

        } else {
            input.SOC_RSV_prev = BigDecimal.ZERO;
            input.SOC_clip_usable = BigDecimal.ZERO;
        }

    }

    /***
     * 計算電池未放電時的預估整體需量 = ( [當下15分鐘時段內已收集到的3分鐘時段需量總和] + RTIP )
     *
     * @param input
     */
    private void CaculateTotalDemandOf3MinDatas(RealTimeControlInputModel input) {
        List<ElectricData> real_time_data_in_quarter = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(
                input.fieldId, DataType.getCode(input.REAL_TIME_DATA_TYPE), input.Control_Quarter_Start,
                input.Control_Start);
        if (real_time_data_in_quarter.size() < input.Whole_3Min - 1) {
            throw new IiiException("當下時段已收集到的3分鐘資料不完整");
        }

        /*
         * 3分鐘缺值的狀況，拿前一個時段的數值做為替補，還沒測試過，先把捕的程式註解
         */
        /*
         * for (int i = 1; i < real_time_data_in_quarter.size(); i++) {
         * if(real_time_data_in_quarter.get(i).getM1kW().compareTo(BigDecimal.ZERO) <=
         * 0) { real_time_data_in_quarter.set(i, real_time_data_in_quarter.get(i-1)); }
         * }
         */
        // 預估最後一筆的需量當作下一筆的需量，再將之前反應過的M1統計，看還要補多少
        BigDecimal Demand_Whole = input.RTIP;
        for (ElectricData data : real_time_data_in_quarter) {
            Demand_Whole = Demand_Whole.add(data.getM1kW());
        }
        input.Demand_Whole = Demand_Whole;
    }

    /***
     * 計算整體需量控制目標 = THDC * whole_3min
     *
     * @param input
     */
    private void DecideControlTarget(RealTimeControlInputModel input) {
        // TODO Auto-generated method stub
        input.Control_Whole = input.THDC.multiply(BigDecimal.valueOf(input.Whole_3Min));
    }

    /***
     * 查詢並計算電池放電即時控制的目標值 = Max( TYOD, RMMD )
     *
     * @param input
     */
    private void DecideTHDC(RealTimeControlInputModel input) {
        BigDecimal THDC = input.TYOD;
        // 決定調度目標， 0:TRHD 1:TYOD
        if (input.field.getTargetType() == 0) {
            THDC = input.TYOD.max(input.RMMD);
        }

        input.THDC = THDC;
    }

    /***
     * 計算即時控制整體考量的X秒鐘時段數量（當下時段開始~當下3分鐘時段結束） = 當下分鐘 % 15 ÷ 3 + 1<br/>
     * 計算當下~當下時段結束的分鐘數 = 15–當下分鐘 % 15<br/>
     * 要調整成秒
     *
     * @param input
     */
    @SuppressWarnings("deprecation")
    private void caculateWholeSectionAmount(RealTimeControlInputModel input) {

        int min = input.Control_Start.getMinutes() % 15;
        int seconds = input.Control_Start.getSeconds();
        int totalSeconds = min * 60 + seconds;
        input.Whole_3Min = totalSeconds / input.timeInterval + 1;
        // input.Quarter_Distance = 15 - this.Control_Start.Minute % 15;
        // //暫時還不改，跟後面呼叫使用有關
        input.Quarter_Distance = Utility.MINUTES_15 - totalSeconds;
    }

    /***
     * 查詢[當月開頭]~[前一時段結尾]時間內的最大實際需量
     *
     * @param input
     */
    @SuppressWarnings("deprecation")
    private void getNewestRMMD(RealTimeControlInputModel input) {
        List<ElectricData> real_month_data_before = electricDataRepository
                .findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(input.fieldId,
                        DataType.getCode(input.REAL_DATA_TYPE), input.Control_Month_Start, input.Control_Start,
                        new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "m1kW")));
        if (real_month_data_before.size() > 0) {
            input.RMMD = real_month_data_before.get(0).getM1kW();
        } else {
            input.RMMD = BigDecimal.ZERO;
        }
    }

    /***
     * 讀取並計算前一3分鐘時段收集到的[原始負載–PV]之即時功率 = 即時需量 + 即時電池放電功率 + 即時冰機卸載功率<br/>
     * 計算電池可用餘量 = Max( 0, SOC–當下時段的排程電池保留餘量 )
     *
     * @param input
     */
    private void getNewestRTIP(RealTimeControlInputModel input) {
        // TODO Auto-generated method stub
        List<ElectricData> real_time_data = electricDataRepository.findByFieldIdAndDataTypeAndTime(input.fieldId,
                DataType.getCode(input.REAL_TIME_DATA_TYPE), input.Control_Start);
        if (real_time_data.size() < 1) {
            throw new IiiException("即時收集到的資料不完整");
        }
        ElectricData RT_Data = real_time_data.get(0);
        input.RTIP = RT_Data.getM1kW().add(RT_Data.getM3kW()).add(RT_Data.getM7kW());
        input.SOC = RT_Data.getMsoc();
        // 使用電池保留量
        if (input.field.getIsReserve()) {
            input.SOC_usable = input.SOC.subtract(input.SOC_RSV);
        } else {
            input.SOC_usable = input.SOC;
        }
        // 確保不為負數
        input.SOC_usable = input.SOC_usable.max(BigDecimal.ZERO);
    }

    /***
     * 查詢[當月開頭]~[前一時段結尾]時間內的最大實際需量
     *
     * @param input
     */
    private void getNewestTDMDAndSocRsv(RealTimeControlInputModel input) {
        Optional<ElectricData> schedule_data_present = input.Schedule_Data.stream()
                                                                          .filter(a -> a.getTime().equals(input.Control_Quarter_End))
                                                                          .findFirst();
        if (!schedule_data_present.isPresent()) {
            throw new IiiException("調度排程資料不完整");
        }
        // TDMD是需量期間的銷峰值
        // this.TDMD = schedule_data_present.get().getT;
        // 取得下一個時段排程的電池保留量
        input.SOC_RSV = schedule_data_present.get().getMsocRsv();
    }

    /***
     * * 讀取當日全天排程資料
     *
     * @param input
     */

    private void LoadScheduleData(RealTimeControlInputModel input) {
        input.Schedule_Data = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(input.fieldId,
                DataType.getCode(input.SCHEDULE_DATA_TYPE), input.Control_Day_Start, input.TODAY_2400);

    }
}
