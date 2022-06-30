package org.iii.esd.benefit;

import lombok.Data;

@Data
public class BenefitSumResult {
    // TOU_Reduce
    private BenefitResult tou_reduce;
    // 基本電費降低效益
    private BenefitResult bou_reduce;
    // 效益總計
    private BenefitResult sum;
}