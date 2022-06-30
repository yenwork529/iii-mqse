package org.iii.esd.schedule;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.NoArgsConstructor;
import lombok.ToString;

import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.utils.DatetimeUtils;

/***
 * 調度排程傳入參數模型，先把冰機以及可控設備、需量反應移除
 *
 * @author iii
 *
 */
@ToString
@NoArgsConstructor
public class SchedulingInputModel {

    public static final BigDecimal Step = BigDecimal.valueOf(0.1);
    public static int NORMAL = 0;
    public static int MUST_DR = 1;
    public static int MUST_DR_OFF = 2;
    /***
     * 排程開頭時間
     */
    public Date Schedule_Start;
    /***
     * 場域代號，同FIELDID
     */
    public long fieldId = -1;
    /***
     * 預測資料對應的DATA_TYPE
     */
    public int FORECAST_DATA_TYPE = -1;
    /***
     * 實際資料對應的DATA_TYPE
     */
    public int REAL_DATA_TYPE = -1;
    /***
     * 排程結果資料對應的DATA_TYPE
     */
    public int SCHEDULE_DATA_TYPE = -1;
    /***
     * 電池最大蓄電容量
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
     * 全年目標最佳契約容量
     */
    public BigDecimal TYOD;
    /***
     * 調度策略
     */
    public PolicyProfile config;

    /* internal */
    /***
     * 排程日的開頭時間
     */
    Date Schedule_Day_Start;
    /***
     * 排程日的結尾時間
     */
    Date Schedule_Day_End;
    /***
     * 排程月的開頭時間
     */
    Date Schedule_Month_Start;

    /***
     * 排程開頭之前的當月最大需量
     */
    BigDecimal RMMD;
    /***
     * Max(TYOD, RMMD)
     */
    BigDecimal TRHD;
    /***
     * Max( TRHD, 排程削峰運算結果中排程目標時間以後的當日最大排程需量 )
     */
    // double TDMD;
    /***
     * 排程日是否為需量競價之抑低用電日
     */
    boolean Is_Suppression_Day;
    /***
     * 排程開頭時間之前的所有抑低時段之需量最大值
     */
    // double Suppressed_Max_Demand = 0;
    /***
     * 需量競價之抑低目標需量值
     */
    // double Suppress_Target;
    /***
     * Customer Baseline Load
     */
    // double CBL;
    /***
     * 抑低開頭時段序號
     */
    // int Suppress_Start_Idx;
    /***
     * 抑低結尾時段序號
     */
    // int Suppress_End_Idx;

    /***
     * 排程開頭時段序號
     */
    int Schedule_Start_Idx;
    /***
     * 排程開頭的電池餘量
     */
    BigDecimal Initial_SOC;

    public SchedulingInputModel(Date Schedule_Start, long Profile_ID, int FORECAST_DATA_TYPE, int REAL_DATA_TYPE,
            int SCHEDULE_DATA_TYPE, BigDecimal Battery_Capacity, BigDecimal Max_Power, BigDecimal Charge_Efficiency,
            BigDecimal Discharge_Efficiency, BigDecimal TYOD, boolean Is_Demand_Bidding_Enabled, PolicyProfile config) {
        Initialize(Schedule_Start, Profile_ID, FORECAST_DATA_TYPE, REAL_DATA_TYPE, SCHEDULE_DATA_TYPE, Battery_Capacity,
                Max_Power, Charge_Efficiency, Discharge_Efficiency, TYOD, Is_Demand_Bidding_Enabled, config);
    }

    public void check() throws Exception {
        if (this.Schedule_Start == null) {
            throw new IiiException("沒有指定排程起始日期");
        }
        if (this.fieldId == -1) {
            throw new IiiException("沒有指定場域ID");
        }
        if (this.FORECAST_DATA_TYPE == -1) {
            throw new IiiException("沒有指定負載預測來源");
        }
        if (this.REAL_DATA_TYPE == -1) {
            throw new IiiException("沒有指定歷史資料來源");
        }
        if (this.SCHEDULE_DATA_TYPE == -1) {
            throw new IiiException("沒有指定排程資料輸出");
        }
        if (this.Battery_Capacity == null) {
            throw new IiiException("沒有指定電池容量");
        }
        if (this.Max_Power == null) {
            throw new IiiException("沒有指定電池出力");
        }
        if (this.Charge_Efficiency == null) {
            throw new IiiException("沒有指定充電效率");
        }
        if (this.Discharge_Efficiency == null) {
            throw new IiiException("沒有指定放電效率");
        }
        if (this.TYOD == null) {
            throw new IiiException("沒有指定TYOD");
        }
        if (this.config == null) {
            throw new IiiException("沒有指定調度策略");
        }
        initial();
    }

    /***
     * 查詢排程開頭以前一天內最近一筆的實際電池餘量，並減去該筆資料時段結束時間至目標時段開始時間內所有時段累計的排程放電量（或加上排程充電量），來當作排程開頭的電池餘量
     *
     * @param historyDatas
     */
    public void UpdateInitialSOC(List<ElectricData> historyDatas, List<ElectricData> schedule_data_between) {
        // 排程資料固定是15分鐘，所以乘以0.25
        BigDecimal toKwh = new BigDecimal(0.25);
        BigDecimal de = (this.Discharge_Efficiency);
        BigDecimal ce = (this.Charge_Efficiency);
        // 按照時間順序排序，取最後一筆
        ElectricData last = historyDatas.get(historyDatas.size() - 1);
        BigDecimal accumDischargedSOC = BigDecimal.ZERO; // 排程資料在期間內累計放出的SOC
        for (ElectricData i : schedule_data_between) {
            if (i.getM3kW().compareTo(BigDecimal.ZERO) > 0) {
                accumDischargedSOC = accumDischargedSOC
                        .add(i.getM3kW().multiply(toKwh).divide(de, 3, BigDecimal.ROUND_HALF_UP));// 排程放電
            } else {
                accumDischargedSOC = accumDischargedSOC.add(i.getM3kW().multiply(toKwh).multiply(ce));// 排程充電
            }
        }

        this.Initial_SOC = last.getMsoc().subtract(accumDischargedSOC);
    }

    /***
     * 轉換掉度策略為銷峰專用
     */
    public void ChangePolicyToPeakClipping() {
        this.TYOD = BigDecimal.ZERO;
        this.config = (PolicyProfile.PeakClippingDefault());
    }

    @SuppressWarnings("deprecation")
    void initial() {
        //		Calendar today = Utility.GetControlDay(Schedule_Start);
        //		Schedule_Day_Start = today.getTime();
        //		Schedule_Day_End = Utility.FixTime(today, 1, 0, 0, 0, 0).getTime();
        //		Schedule_Month_Start = Utility.GetControlMonth1st(Schedule_Day_Start).getTime();

        Schedule_Day_Start = DatetimeUtils.truncated(Schedule_Start, Calendar.DATE);
        Schedule_Day_End = DatetimeUtils.add(Schedule_Day_Start, Calendar.DATE, 1);
        Schedule_Month_Start = DatetimeUtils.truncated(Schedule_Day_Start, Calendar.MONTH);

        // 設定排程開頭時段序號
        Schedule_Start_Idx = Schedule_Start.getHours() * 4 + Schedule_Start.getMinutes() / 15;
        // 先假設非排程日
        Is_Suppression_Day = false;
        Initial_SOC = BigDecimal.ZERO;
        RMMD = BigDecimal.ZERO;
        TRHD = BigDecimal.ZERO;
    }

    private void Initialize(Date Schedule_Start, long Profile_ID, int FORECAST_DATA_TYPE, int REAL_DATA_TYPE,
            int SCHEDULE_DATA_TYPE, BigDecimal Battery_Capacity, BigDecimal Max_Power, BigDecimal Charge_Efficiency,
            BigDecimal Discharge_Efficiency, BigDecimal TYOD, boolean Is_Demand_Bidding_Enabled, PolicyProfile config) {
        this.Schedule_Start = Schedule_Start;
        this.fieldId = Profile_ID;
        this.FORECAST_DATA_TYPE = FORECAST_DATA_TYPE;
        this.REAL_DATA_TYPE = REAL_DATA_TYPE;
        this.SCHEDULE_DATA_TYPE = SCHEDULE_DATA_TYPE;
        this.Battery_Capacity = Battery_Capacity;
        this.Max_Power = Max_Power;
        this.Charge_Efficiency = Charge_Efficiency;
        this.Discharge_Efficiency = Discharge_Efficiency;
        this.TYOD = TYOD;
        this.config = config;
        initial();
    }

    public Date getSchedule_Start() {
        return Schedule_Start;
    }

    public void setSchedule_Start(Date schedule_Start) {
        Schedule_Start = schedule_Start;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public int getFORECAST_DATA_TYPE() {
        return FORECAST_DATA_TYPE;
    }

    public void setFORECAST_DATA_TYPE(int fORECAST_DATA_TYPE) {
        FORECAST_DATA_TYPE = fORECAST_DATA_TYPE;
    }

    public int getREAL_DATA_TYPE() {
        return REAL_DATA_TYPE;
    }

    public void setREAL_DATA_TYPE(int rEAL_DATA_TYPE) {
        REAL_DATA_TYPE = rEAL_DATA_TYPE;
    }

    public int getSCHEDULE_DATA_TYPE() {
        return SCHEDULE_DATA_TYPE;
    }

    public void setSCHEDULE_DATA_TYPE(int sCHEDULE_DATA_TYPE) {
        SCHEDULE_DATA_TYPE = sCHEDULE_DATA_TYPE;
    }

    public BigDecimal getBattery_Capacity() {
        return Battery_Capacity;
    }

    public void setBattery_Capacity(BigDecimal battery_Capacity) {
        Battery_Capacity = battery_Capacity;
    }

    public BigDecimal getMax_Power() {
        return Max_Power;
    }

    public void setMax_Power(BigDecimal max_Power) {
        Max_Power = max_Power;
    }

    public BigDecimal getCharge_Efficiency() {
        return Charge_Efficiency;
    }

    public void setCharge_Efficiency(BigDecimal charge_Efficiency) {
        Charge_Efficiency = charge_Efficiency;
    }

    public BigDecimal getDischarge_Efficiency() {
        return Discharge_Efficiency;
    }

    public void setDischarge_Efficiency(BigDecimal discharge_Efficiency) {
        Discharge_Efficiency = discharge_Efficiency;
    }

    public BigDecimal getTYOD() {
        return TYOD;
    }

    public void setTYOD(BigDecimal tYOD) {
        TYOD = tYOD;
    }

    public PolicyProfile getConfig() {
        return config;
    }

    public void setConfig(PolicyProfile config) {
        this.config = config;
    }

}
