package org.iii.esd.benefit;

import lombok.Data;

@Data
public class BenefitResult {
    // 太陽能
    private double pv;
    // 儲能
    private double ess;
    // 可控設備
    private double unload;
    // 總計
    private double total;
    // 調度後金額
    private double dispatch_bill;
    // 調度前金額
    private double original_bill;
}