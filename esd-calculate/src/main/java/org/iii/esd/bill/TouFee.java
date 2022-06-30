package org.iii.esd.bill;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TouFee {

    /**
     * M2總流動電費
     */
    BigDecimal Total_Bill_M2_KWH = BigDecimal.ZERO;
    /**
     * M3總流動電費
     */
    BigDecimal Total_Bill_M3_KWH = BigDecimal.ZERO;
    /**
     * M7總流動電費
     */
    BigDecimal Total_Bill_M7_KWH = BigDecimal.ZERO;
    /**
     * M2+3+7總流動電費
     */
    BigDecimal Total_Bill_M2_M3_M7_KWH = BigDecimal.ZERO;
    /**
     * M1總流動電費
     */
    BigDecimal Total_Bill_M1_KWH = BigDecimal.ZERO;
    /**
     * M5+6+7-3-2總流動電費
     */
    BigDecimal Total_Bill_A_KWH = BigDecimal.ZERO;
    /**
     * M5+6+7-3總流動電費
     */
    BigDecimal Total_Bill_A2_KWH = BigDecimal.ZERO;
    /**
     * M5+6+7總流動電費
     */
    BigDecimal Total_Bill_B_KWH = BigDecimal.ZERO;

    public void AddFee(TouFee fee) {
        this.Total_Bill_A_KWH = this.Total_Bill_A_KWH.add(fee.Total_Bill_A_KWH);
        this.Total_Bill_A2_KWH = this.Total_Bill_A2_KWH.add(fee.Total_Bill_A2_KWH);
        this.Total_Bill_B_KWH = this.Total_Bill_B_KWH.add(fee.Total_Bill_B_KWH);
        this.Total_Bill_M1_KWH = this.Total_Bill_M1_KWH.add(fee.Total_Bill_M1_KWH);
        this.Total_Bill_M2_KWH = this.Total_Bill_M2_KWH.add(fee.Total_Bill_M2_KWH);
        this.Total_Bill_M3_KWH = this.Total_Bill_M3_KWH.add(fee.Total_Bill_M3_KWH);
        this.Total_Bill_M7_KWH = this.Total_Bill_M7_KWH.add(fee.Total_Bill_M7_KWH);
        this.Total_Bill_M2_M3_M7_KWH = this.Total_Bill_M2_M3_M7_KWH.add(fee.Total_Bill_M2_M3_M7_KWH);
    }
}
