package org.iii.esd.benefit;

import java.util.Date;

import lombok.Data;

/***
 * 對應原C#版本Smart_Dispatching專案的BenefitAnalysis相關演算法，<br>
 * 參考Smart_Dispatching\BenefitAnalysis\Model\Benefit.cs進行改寫<br>
 * 改版異動：<br>
 * - 變數名稱改首字小寫&駝峰式命名<br> 
 * - 移除需量反應相關變數
 * - 相關model引入lombok並改為JavaBean類別
 * @author willhahn
 *
 */
@Data
public class BenefitInputModel {
    // 場域ID
    private long profileId;
    // 計算流動／基本電費降低效益時所針對的資料曲線類別
    private int billingDataType;
    // 效益計算開始日期（年/月/日 00:00:00）
    private Date startDate;
    // 效益計算結束日期（年/月/日 00:00:00，含當日）
    private Date endDate;
    // 儲能設備放電容量
    private double m3Capacity;
    // 可控負載用電容量
    private double m6Capacity;
    // 可控負載卸載容量
    private double m7Capacity;
    // 「基本電費單價」相對於目前價格同步變化的比率
    private double priceRatioBOU;
    // 「尖峰電價」、「半尖峰電價」相對於目前價格同步變化的比率
    private double priceRatioPeak;
    // 「離峰電價」相對於目前價格同步變化的比率
    private double priceRatioOffPeak;
    // ESS自放電補償係數，預設為0
    private double essSelfKWhComp;
    // ESS轉換效率補償係數，預設為1
    private double essEffiComp;

    /* internal */
    // 總基本電費扣減
    private double bouDeduction = 0;
    // 總流動電費扣減
    private double touDeduction = 0;
    // 總基本電費扣減 + 總流動電費扣減
    private double totalDeduction = 0;

    public BenefitInputModel(long profileId, int billingDataType, Date startDate, Date endDate, double m3Capacity,
            double m6Capacity, double m7Capacity, double priceRatioBOU, double priceRatioPeak, double priceRatioOffPeak,
            double essSelfKWhComp, double essEffiComp) {
        this.profileId = profileId;
        this.billingDataType = billingDataType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.m3Capacity = m3Capacity;
        this.m6Capacity = m6Capacity;
        this.m7Capacity = m7Capacity;
        this.priceRatioBOU = priceRatioBOU;
        this.priceRatioPeak = priceRatioPeak;
        this.priceRatioOffPeak = priceRatioOffPeak;
        this.essSelfKWhComp = essSelfKWhComp;
        this.essEffiComp = essEffiComp;
    }
}