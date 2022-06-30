package org.iii.esd.control;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.caculate.Utility;

/***
 * Smart_Dispatching.ESS
 *
 * @author iii
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EssDevice {
    /***
     * 一小時共4個15分鐘時段T1/T2/T3/T4
     */
    public static final int TIMESLOT_LEN = 4;
    /**
     * 蓄電量(量測值)kwh
     */
    public BigDecimal[] storageOperation = new BigDecimal[TIMESLOT_LEN];
    /***
     * 電池蓄電容量kwh
     */
    public BigDecimal BatteryCapacity;
    /***
     * 電池最大充/放電功率kw
     */
    public BigDecimal MaxPower;
    /***
     * 電池充電效率 = 電氣變換效率*電池轉換效率
     */
    public BigDecimal ChargeEfficiency;
    /***
     * 電池放電效率 = 電氣變換效率*電池轉換效率
     */
    public BigDecimal DischargeEfficiency;
    /***
     * 電池餘量(蓄電量)
     */
    public BigDecimal MSOC;
    /***
     * 控制間距為幾小時
     */
    public BigDecimal hours;

    public EssDevice(BigDecimal msoc, BigDecimal batterCapacity, BigDecimal maxPower, BigDecimal ChargeEfficiency,
            BigDecimal DischargeEfficiency, BigDecimal hours) {
        this.MSOC = msoc;
        this.BatteryCapacity = batterCapacity;
        this.MaxPower = maxPower;
        this.ChargeEfficiency = ChargeEfficiency;
        this.DischargeEfficiency = DischargeEfficiency;
        this.hours = hours;
    }

    public BigDecimal GetChargeable(BigDecimal controllable) {
        // 計算3分鐘時段待充電的功率總和 = (電池蓄電容量–SOC ) ÷ 電池充電效率 ÷ 0.05 h
        BigDecimal toCharge = (this.BatteryCapacity.subtract(this.MSOC))
                .divide(this.ChargeEfficiency, 3, BigDecimal.ROUND_HALF_UP).divide(this.hours, 3, BigDecimal.ROUND_HALF_UP);
        // 將[控制電池充電功率]設為( -1 × Max( 0, Min( to_charge, 電池最大充電功率, controllable ) ) )
        // return -Math.Max(0, Math.Min(toCharge, Math.Min(this.MaxPower,
        // Math.Abs(controllable))));
        return controllable.abs().min(this.MaxPower).min(toCharge).max(BigDecimal.ZERO).multiply(Utility.minus);
    }

    public BigDecimal GetDischargeable(BigDecimal controllable) {
        // 計算3分鐘時段可放電的功率總和 = SOC × 電池放電效率 ÷ 0.05 h
        BigDecimal toDischarge = MSOC.multiply(this.DischargeEfficiency).divide(this.hours, 3, BigDecimal.ROUND_HALF_UP);
        // 將[控制電池放電功率]設為Max( 0, Min( toDischarge, 電池最大放電功率, controllable ) )
        //return Math.Max(0, Math.Min(toDischarge, Math.Min(this.MaxPower, controllable)));
        return controllable.min(this.MaxPower).min(toDischarge).max(BigDecimal.ZERO);
    }

    public BigDecimal GetNextControllable(BigDecimal diffValue) {
        if (diffValue.compareTo(BigDecimal.ZERO) < 0) // 若蓄電量差值(後減前)小於0表示需要放電
        {
            return (diffValue.multiply(Utility.minus).multiply(this.DischargeEfficiency))
                    .divide(Utility.toKwh, 3, BigDecimal.ROUND_HALF_UP); // 計算下個時段的放電需量
        } else if (diffValue.compareTo(BigDecimal.ZERO) > 0) // 若蓄電量差值(後減前)大於0表示需要充電
        {
            return (diffValue.multiply(Utility.minus).multiply(this.ChargeEfficiency))
                    .divide(Utility.toKwh, 3, BigDecimal.ROUND_HALF_UP); // 計算下個時段的充電需量
        } else // 等於0表示不充不放
        { return BigDecimal.ZERO; }
    }
}
