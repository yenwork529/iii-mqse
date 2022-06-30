package org.iii.esd.bill;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class BouFee {

    /**
     * M1基本電費
     */
    BigDecimal BOU_Bill_M1 = BigDecimal.ZERO;
    /**
     * M1+7基本電費
     */
    BigDecimal BOU_Bill_M1_M7 = BigDecimal.ZERO;
    /**
     * M1+2+7基本電費
     */
    BigDecimal BOU_Bill_M1_M2_M7 = BigDecimal.ZERO;
    /**
     * M1+2+3+7基本電費
     */
    BigDecimal BOU_Bill_M1_M2_M3_M7 = BigDecimal.ZERO;
    /**
     * M1 PF調整費
     */
    BigDecimal PF_Bill_M1 = BigDecimal.ZERO;
    /**
     * M1+7 PF調整費
     */
    BigDecimal PF_Bill_M1_M7 = BigDecimal.ZERO;
    /**
     * M1+2+7 PF調整費
     */
    BigDecimal PF_Bill_M1_M2_M7 = BigDecimal.ZERO;
    /**
     * M1+2+7+7 PF調整費
     */
    BigDecimal PF_Bill_M1_M2_M3_M7 = BigDecimal.ZERO;
}
