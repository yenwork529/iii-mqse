/**
 *
 */
package org.iii.esd.control;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.iii.esd.caculate.Utility;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;

/**
 * @author iii
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RealTimeControlInputModel {


    /***
     * 資料時間間距，單位為秒，預設為180，還沒肯定要到秒，但是假如因應輔助服務秒級的話，參數設做秒會比較好些
     */
    public int timeInterval = 180;

    /***
     * 即時控制的開頭時間
     */
    public Date Control_Start;
    /***
     * 場域代號
     */
    public long fieldId;
    /***
     * 全年目標最佳契約容量
     */
    public BigDecimal TYOD;
    /***
     * 即時收集資料對應的DATA_TYPE
     */
    public int REAL_TIME_DATA_TYPE;
    /***
     * 實際歷史資料對應的DATA_TYPE
     */
    public int REAL_DATA_TYPE;
    /***
     * 排程結果資料對應的DATA_TYPE
     */
    public int SCHEDULE_DATA_TYPE;
    /***
     * 電池蓄電容量
     */
    public BigDecimal Battery_Capacity;
    /***
     * 電池最大充／放電功率
     */
    public BigDecimal Max_Power;
    /***
     * 電池充電效率 = 電氣變換效率*電池轉換效率
     */
    public BigDecimal Charge_Efficiency;
    /***
     * 電池放電效率 = 電氣變換效率*電池轉換效率
     */
    public BigDecimal Discharge_Efficiency;
    /***
     * 調度策略
     */
    public PolicyProfile config;

    /* internal */
    /***
     * 即時控制的當下15分鐘時段開頭時間
     */
    Date Control_Quarter_Start;
    /***
     * 即時控制的當下15分鐘時段結束時間
     */
    Date Control_Quarter_End;
    /***
     * 即時控制的當日開頭時間
     */
    Date Control_Day_Start;
    /***
     * 即時控制的當月開頭時間
     */
    Date Control_Month_Start;
    /***
     * 即時控制整體考量的3分鐘時段數量
     */
    int Whole_3Min;
    /***
     * 當下距離當下時段結束的分鐘數<br/>
     * 嘗試把它改成秒數
     */
    int Quarter_Distance;
    /***
     * 實際收集到的[原始負載–PV]之即時功率
     */
    BigDecimal RTIP;
    /***
     * [當月開頭]~[前一時段結尾]時間內的最大實際需量
     */
    BigDecimal RMMD;
    /***
     * 所屬15分鐘時段的排程TDMD
     */
    BigDecimal TDMD;
    /***
     * 電池放電即時控制的目標值
     */
    BigDecimal THDC;
    /***
     * 整體需量控制目標
     */
    BigDecimal Control_Whole;
    /***
     * 電池未放電時的預估整體需量
     */
    BigDecimal Demand_Whole;
    /***
     * 15分鐘已發生之需量(過去15分鐘已發生整體需量，在00/15/30/45分時使用RTIP的值，其他時間則為各TimeSlot淨負載累加值)
     */
    BigDecimal Demand_Past;
    /***
     * 15分鐘已發生之平均需量(過去15分鐘已發生整體需量之平均值，與Demand_Whole加總已發生需量和控制當下3分鐘預估需量不同)
     */
    BigDecimal Demand_Past_Average;
    /***
     * 當下時段的電池保留餘量
     */
    BigDecimal SOC_RSV;
    /***
     * 前一時段的電池保留餘量
     */
    BigDecimal SOC_RSV_prev;
    /***
     * 即時電池餘量
     */
    BigDecimal SOC;
    /***
     * 電池可用餘量
     */
    BigDecimal SOC_usable;
    /***
     * 電池削峰可用餘量
     */
    BigDecimal SOC_clip_usable;
    /***
     * 當日是否為需量競價之抑低用電日
     */
    boolean Is_Suppression_Day;
    /***
     * 當下是否為需量競價之抑低用電期間
     */
    boolean Is_Suppression_Period;
    /***
     * 當下時段之前的所有抑低時段之需量最大值
     */
    BigDecimal Suppressed_Max_Demand = BigDecimal.ZERO;
    /***
     * 需量競價之抑低目標需量
     */
    BigDecimal Suppress_Target;
    /***
     * 抑低開始時間
     */
    Date Suppress_Start;
    /***
     * 抑低結束時間
     */
    Date Suppress_End;

    Date TODAY_0730;

    Date TODAY_1315;

    Date TODAY_1330;

    Date TODAY_1445;

    Date TODAY_1500;

    Date TODAY_1700;

    Date TODAY_2400;
    /***
     * 當日所有排程資料
     */
    List<ElectricData> Schedule_Data;

    FieldProfile field;

    /***
     * 設定各項參數檢查的方法
     */
    public void CheckParameter() {
        Initial();
    }

    /***
     * 控制頻率轉小時，3分鐘=0.05H之類的
     * @return
     */
    public BigDecimal GetHoursInterval() {
        return BigDecimal.valueOf(timeInterval).divide(BigDecimal.valueOf(Utility.MINUTES_60), 5, BigDecimal.ROUND_HALF_UP);
    }

    /***
     * 初始化執行時內部參數
     */
    private void Initial() {
        Calendar controlStart = Utility.GetControlStart(Control_Start);

        Calendar today = Utility.GetControlDay(Control_Start);

        Calendar month1st = Utility.GetControlMonth1st(Control_Start);

        // 設定當日特定時間
        TODAY_0730 = Utility.FixTime(today, 0, 7, 30, 0, 0).getTime();
        TODAY_1315 = Utility.FixTime(today, 0, 13, 15, 0, 0).getTime();
        TODAY_1330 = Utility.FixTime(today, 0, 13, 30, 0, 0).getTime();
        TODAY_1445 = Utility.FixTime(today, 0, 14, 45, 0, 0).getTime();
        TODAY_1500 = Utility.FixTime(today, 0, 15, 00, 0, 0).getTime();
        TODAY_1700 = Utility.FixTime(today, 0, 17, 30, 0, 0).getTime();
        TODAY_2400 = Utility.FixTime(today, 1, 0, 0, 0, 0).getTime();
        // 設定即時控制的當日／當月之開頭時間

        Control_Day_Start = today.getTime();
        Control_Month_Start = month1st.getTime();
        int hour = controlStart.get(Calendar.HOUR_OF_DAY);
        int minutes = controlStart.get(Calendar.MINUTE);
        minutes = minutes - minutes % 15;
        //調整至15分齊
        Control_Quarter_Start = Utility.FixTime(today, 0, hour, minutes, 0, 0).getTime();
        Control_Quarter_End = Utility.FixTime(today, 0, hour, minutes + 15, 0, 0).getTime();

    }

}
