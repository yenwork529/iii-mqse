package org.iii.esd.benefit;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.bill.BillServiceFactory;
import org.iii.esd.bill.BouFee;
import org.iii.esd.bill.IBillService;
import org.iii.esd.bill.TouFee;
import org.iii.esd.caculate.Utility;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.TouType;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.mongo.service.FieldProfileService;

/***
 * 對應原C#版本Smart_Dispatching專案的BenefitAnalysis相關演算法，<br>
 * 參考Smart_Dispatching\BenefitAnalysis\Benefit.cs進行改寫<br>
 * 改版異動：<br>
 * - 方法名稱改首字小寫&駝峰式命名<br>
 * - 移除需量反應相關演算法
 *
 * @author willhahn
 *
 */
@Service
@Log4j2
public class BenefitService {

    public final TouType DEFAULT_TOU_TYPE = TouType.TPH3S;
    @Autowired
    private BillServiceFactory billServiceFactory;
    @Autowired
    private FieldProfileService fieldProfileService;
    @Autowired
    private ElectricDataRepository electricDataRepository;
    @Autowired
    private ElectricDataService electricDataService;

    /***
     * 計算流動電費
     *
     * @param BenefitInputModel
     * @return BenefitResult
     * @throws Exception
     */
    public BenefitResult getTOUReduce(BenefitInputModel model) {
        Optional<FieldProfile> fieldProfile = fieldProfileService.find(model.getProfileId());
        if (!fieldProfile.isPresent()) {
            return null;
        }

        TouType touType = fieldProfile.get().getTouType();
        IBillService billService = billServiceFactory.GetInstance(touType != null ? touType : DEFAULT_TOU_TYPE);
        TouFee touFee = billService.TimeOfUsed_Bill(model.getStartDate(), model.getEndDate(), model.getProfileId(),
                model.getBillingDataType(), model.getPriceRatioPeak(), model.getPriceRatioOffPeak(),
                model.getEssSelfKWhComp(), model.getEssEffiComp());

        BenefitResult benefitResult = new BenefitResult();
        benefitResult.setPv(touFee.getTotal_Bill_M2_KWH().doubleValue());
        benefitResult.setEss(touFee.getTotal_Bill_M3_KWH().doubleValue());
        benefitResult.setUnload(touFee.getTotal_Bill_M7_KWH().doubleValue());
        benefitResult.setOriginal_bill(touFee.getTotal_Bill_B_KWH().doubleValue());
        benefitResult.setDispatch_bill(touFee.getTotal_Bill_M1_KWH().doubleValue());
        benefitResult.setTotal(touFee.getTotal_Bill_B_KWH().subtract(touFee.getTotal_Bill_M1_KWH()).doubleValue());
        return benefitResult;
    }

    /***
     * 新版[基本電費降低效益]分配方法（各月各裝置依照「PV→ESS→Unload」的貢獻順序各自對最高需量的降幅來等比例均分總效益。
     * 暫不另外處理合計效益為負的狀況
     *
     * @param BenefitInputModel
     * @return BenefitResult
     */
    public BenefitResult getBOUReduce(BenefitInputModel model) {
        Optional<FieldProfile> fieldProfile = fieldProfileService.find(model.getProfileId());
        if (!fieldProfile.isPresent()) {
            return null;
        }

        TouType touType = fieldProfile.get().getTouType();
        IBillService billService = billServiceFactory.GetInstance(touType != null ? touType : DEFAULT_TOU_TYPE);
        BenefitResult benefitResult = new BenefitResult();
        double pv = 0;
        double ess = 0;
        double unload = 0;
        double total = 0;
        double dispatchBill = 0;
        double originalBill = 0;

        // 依期間內各月分別計算
        for (Date targetMonthStart = Utility.getFirstDate(model.getStartDate().getTime());
                targetMonthStart.getTime() <= model.getEndDate().getTime(); targetMonthStart = Utility.addMonths(targetMonthStart, 1)) {
            log.info("\n目標月份：" + targetMonthStart);
            Date targetMonthEnd = Utility.addMonths(targetMonthStart, 1); // 當月最後一天結束時間
            Date targetMonthEndDay = Utility.addDays(targetMonthEnd, -1); // 當月最後一天

            List<ElectricData> electricDataList = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(
                    model.getProfileId(), DataType.getCode(model.getBillingDataType()), targetMonthStart,
                    targetMonthEnd);
            int month_dataCount = electricDataList.size();

            log.info("當月資料筆數：" + month_dataCount);
            if (month_dataCount <= 0) { continue; }

            // 找出當月各種曲線的最大功率
            double Max_KW_M1_2_3_7; // [原始負載]最大功率
            double Max_KW_M1_3_7; // [原始負載-PV]最大功率
            double Max_KW_M1_7; // [原始負載-PV-ESS]最大功率
            double Max_KW_M1; // [原始負載-PV-ESS-冰機卸載]最大功率

            Max_KW_M1_2_3_7 = electricDataService
                    .FindMaxM1addM2addM3addM7InTimeRangeInDocument(model.getProfileId(), DataType.getCode(model.getBillingDataType()),
                            targetMonthStart, targetMonthEnd).getKW().doubleValue();

            Max_KW_M1_3_7 = electricDataService
                    .FindMaxM1addM3addM7InTimeRangeInDocument(model.getProfileId(), DataType.getCode(model.getBillingDataType()),
                            targetMonthStart, targetMonthEnd).getKW().doubleValue();

            Max_KW_M1_7 = electricDataService
                    .FindMaxM1addM7InTimeRange(model.getProfileId(), DataType.getCode(model.getBillingDataType()), targetMonthStart,
                            targetMonthEnd).getKW().doubleValue();

            Max_KW_M1 = electricDataService
                    .FindMaxM1InTimeRangeInDocument(model.getProfileId(), DataType.getCode(model.getBillingDataType()), targetMonthStart,
                            targetMonthEnd).getKW().doubleValue();

            // 依照「PV→ESS→Unload」的順序計算各種設備對最高需量降低的貢獻比例
            double totalMaxKW = Max_KW_M1_2_3_7 - Max_KW_M1;
            if (totalMaxKW == 0) {
                totalMaxKW = 1; // 避免除數為0的狀況
            }
            double M2_ratio = (Max_KW_M1_2_3_7 - Max_KW_M1_3_7) / totalMaxKW;
            double M3_ratio = (Max_KW_M1_3_7 - Max_KW_M1_7) / totalMaxKW;
            double M7_ratio = (Max_KW_M1_7 - Max_KW_M1) / totalMaxKW;

            // 取得當月合計的[基本電費降低效益]
            double totalBenefit;
            Date startDay = targetMonthStart.before(model.getStartDate()) ? model.getStartDate() : targetMonthStart; // 當月在期間內的起始日
            Date endDay = targetMonthEndDay.after(model.getEndDate()) ? model.getEndDate() : targetMonthEndDay; // 當月在期間內的結束日

            BouFee bouFee = billService
                    .BOU_PF_Bill(startDay, endDay, model.getProfileId(), model.getBillingDataType(), model.getPriceRatioBOU(),
                            model.getPriceRatioPeak(), model.getPriceRatioOffPeak(), model.getEssSelfKWhComp(), model.getEssEffiComp());
            totalBenefit = bouFee.getBOU_Bill_M1_M2_M3_M7().subtract(bouFee.getPF_Bill_M1_M2_M3_M7()).subtract(bouFee.getBOU_Bill_M1())
                                 .subtract(bouFee.getPF_Bill_M1()).doubleValue();

            // 依設備的貢獻比例均分合計效益
            pv += totalBenefit * M2_ratio;
            ess += totalBenefit * M3_ratio;
            unload += totalBenefit * M7_ratio;
            total += totalBenefit;
            dispatchBill += bouFee.getBOU_Bill_M1().subtract(bouFee.getPF_Bill_M1()).doubleValue();
            originalBill += bouFee.getBOU_Bill_M1_M2_M3_M7().subtract(bouFee.getPF_Bill_M1_M2_M3_M7()).doubleValue();
        }
        benefitResult.setPv(pv);
        benefitResult.setEss(ess);
        benefitResult.setUnload(unload);
        benefitResult.setTotal(total);
        benefitResult.setDispatch_bill(dispatchBill);
        benefitResult.setOriginal_bill(originalBill);

        return benefitResult;
    }

    /***
     * 效益合計計算
     *
     * @param BenefitInputModel
     * @return BenefitSumResult
     */
    public BenefitSumResult getSum(BenefitInputModel model) {
        BenefitSumResult bsr = new BenefitSumResult();
        bsr.setTou_reduce(getTOUReduce(model));
        bsr.setBou_reduce(getBOUReduce(model));

        if (bsr.getTou_reduce() == null || bsr.getBou_reduce() == null) { return null; }

        bsr.setSum(new BenefitResult());
        bsr.getSum().setPv(bsr.getTou_reduce().getPv() + bsr.getBou_reduce().getPv());
        bsr.getSum().setEss(bsr.getTou_reduce().getEss() + bsr.getBou_reduce().getEss());
        bsr.getSum().setUnload(bsr.getTou_reduce().getUnload() + bsr.getBou_reduce().getUnload());
        bsr.getSum().setTotal(bsr.getTou_reduce().getTotal() + bsr.getBou_reduce().getTotal());
        bsr.getSum().setDispatch_bill(bsr.getTou_reduce().getDispatch_bill() + bsr.getBou_reduce().getDispatch_bill());
        bsr.getSum().setOriginal_bill(bsr.getTou_reduce().getOriginal_bill() + bsr.getBou_reduce().getOriginal_bill());

        return bsr;
    }
}
