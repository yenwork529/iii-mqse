package org.iii.esd.bill;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.caculate.Utility;
import org.iii.esd.enums.DataType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.AbstractTou;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.TouOfTPH3S;
import org.iii.esd.mongo.domain.ElectricDataAggregateResult;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.repository.FieldProfileRepository;
import org.iii.esd.mongo.repository.TouOfTPH3SRepository;
import org.iii.esd.mongo.service.ElectricDataService;

@Service
@Log4j2
public class Tph3sBillService implements IBillService {
    @Autowired
    TouOfTPH3SRepository touOfTPH3SRepository;
    @Autowired
    ElectricDataRepository electricDataRepository;
    @Autowired
    FieldProfileRepository fieldProfileRepository;
    @Autowired
    ElectricDataService electricDataService;

    @SuppressWarnings("deprecation")
    protected BouPartFee GetM1Bou(Date from, Date until, long fieldId, DataType datatype, TouFee touFee,
            BigDecimal tyodc, BigDecimal Regular_Contract_Cost, BigDecimal monthRate, BigDecimal PF_Reduce_Percent) {
        // 取當月最高需量max kw
        List<ElectricData> m1eds = electricDataRepository.findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(fieldId,
                datatype, from, until, new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "m1kW")));
        BigDecimal month_maxkw = m1eds.get(0).getM1kW();
        return UseKwCaculateBou(month_maxkw, touFee.getTotal_Bill_M1_KWH(), tyodc, Regular_Contract_Cost, monthRate, PF_Reduce_Percent);
    }

    protected BouPartFee GetM1_M7Bou(Date from, Date until, long fieldId, DataType datatype, TouFee touFee,
            BigDecimal tyodc, BigDecimal Regular_Contract_Cost, BigDecimal monthRate, BigDecimal PF_Reduce_Percent) {
        // 取當月最高需量max kw
        ElectricDataAggregateResult m1_m7 = electricDataService.FindMaxM1addM7InTimeRange(fieldId, datatype, from,
                until);
        return UseKwCaculateBou(m1_m7.getKW(), touFee.getTotal_Bill_M1_KWH().add(touFee.getTotal_Bill_M7_KWH()), tyodc,
                Regular_Contract_Cost, monthRate, PF_Reduce_Percent);
    }

    protected BouPartFee GetM1_M2_M3_M7Bou(Date from, Date until, long fieldId, DataType datatype, TouFee touFee,
            BigDecimal tyodc, BigDecimal Regular_Contract_Cost, BigDecimal monthRate, BigDecimal PF_Reduce_Percent) {
        // 取當月最高需量max kw
        ElectricDataAggregateResult m1_m7 = electricDataService.FindMaxM1addM2addM3addM7InTimeRange(fieldId, datatype,
                from, until);
        return UseKwCaculateBou(m1_m7.getKW(), touFee.getTotal_Bill_M1_KWH().add(touFee.getTotal_Bill_M2_M3_M7_KWH()), tyodc,
                Regular_Contract_Cost, monthRate, PF_Reduce_Percent);
    }

    protected BouPartFee UseKwCaculateBou(BigDecimal maxKw, BigDecimal totalTouFee, BigDecimal tyodc,
            BigDecimal Regular_Contract_Cost, BigDecimal monthRate, BigDecimal PF_Reduce_Percent) {
        BouPartFee fee = new BouPartFee();
        // 基本電費部分
        fee.Regular_Contrat_Bill = Regular_Contract_Cost.multiply(tyodc);
        // 超約部分
        fee.Over_Contract_Bill = Calculate_OverCost(maxKw, tyodc, Regular_Contract_Cost);
        // 總基本電費
        fee.BOU_Bill = (fee.Regular_Contrat_Bill.add(fee.Over_Contract_Bill).multiply(monthRate));
        // PF扣減費
        if (fee.Over_Contract_Bill.compareTo(BigDecimal.ZERO) > 0) {

        } else {
            fee.PF_Bill = (((fee.Regular_Contrat_Bill.add(totalTouFee)).multiply((PF_Reduce_Percent)))
                    .multiply(monthRate));
        }
        log.trace(fee);
        return fee;
    }

    protected BigDecimal Calculate_OverCost(BigDecimal Month_Max_kW, BigDecimal Contract, BigDecimal Price) {
        BigDecimal OverCost = BigDecimal.ZERO;
        if (Month_Max_kW.compareTo(Contract) > 0) {
            BigDecimal Over_10_up_Contract = BigDecimal.ZERO;
            BigDecimal Over_10_low_Contract = BigDecimal.ZERO;
            BigDecimal Contract110 = Contract.multiply(BigDecimal.valueOf(1.1));
            if (Month_Max_kW.compareTo(Contract110) > 0) {
                Over_10_up_Contract = Month_Max_kW.subtract(Contract110); // 超過10%的部分
            }
            // 超過經常契約10%以下的部分
            Over_10_low_Contract = Month_Max_kW.subtract(Contract).subtract(Over_10_up_Contract);
            // 超約電費=10%以下(2倍)+超過10%(3倍)
            OverCost = (Price.multiply(BigDecimal.valueOf(2)).multiply(Over_10_low_Contract))
                    .add((Price.multiply(BigDecimal.valueOf(3)).multiply(Over_10_up_Contract)));
        }
        return OverCost;
    }

    @Override
    public TouFee TimeOfUsed_Bill(Date Start_Day, Date End_Day, long Profile_id, int Data_Type, double Price_Ratio_Peak,
            double Price_Ratio_OffPeak, double ESS_Self_kWh, double ESS_Effi) {
        return TimeOfUsed_Bill(Start_Day, End_Day, Profile_id, DataType.getCode(Data_Type),
                BigDecimal.valueOf(Price_Ratio_Peak), BigDecimal.valueOf(Price_Ratio_OffPeak),
                BigDecimal.valueOf(ESS_Self_kWh), BigDecimal.valueOf(ESS_Effi));
    }

    @Override
    public TouFee TimeOfUsed_Bill(Date Start_Day, Date End_Day, long Profile_id, DataType Data_Type,
            BigDecimal Price_Ratio_Peak, BigDecimal Price_Ratio_OffPeak, BigDecimal ESS_Self_kWh, BigDecimal ESS_Effi) {
        TouFee touFee = new TouFee();

        Calendar startDay = Utility.GetControlDay(Start_Day);
        Calendar endDay = Utility.GetControlDay(End_Day);
        endDay.add(Calendar.DAY_OF_YEAR, 1);

        List<TouOfTPH3S> tous = touOfTPH3SRepository.findAll();

        Calendar walkMonth = Utility.GetControlMonth1st(Start_Day);
        while (walkMonth.compareTo(endDay) < 0) {

            Calendar month1st = (Calendar) walkMonth.clone();

            walkMonth.add(Calendar.MONTH, 1);
            Calendar nextmonth = (Calendar) walkMonth.clone();

            // 依照日期區間決定使用電價版本
            Optional<TouOfTPH3S> ouseTou = tous.stream()
                                               .filter(a -> a.getActiveTime().compareTo(month1st.getTime()) <= 0)
                                               .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
            if (!ouseTou.isPresent()) {
                throw new IiiException("查無對應區間電價");
            }
            TouOfTPH3S useTou = ouseTou.get();

            // 決定用電資料計算區間
            Calendar sectionStart = startDay.compareTo(month1st) >= 0 ? startDay : month1st;
            Calendar sectionEnd = nextmonth.compareTo(endDay) < 0 ? nextmonth : endDay;
            List<ElectricData> eds = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(Profile_id, Data_Type,
                    sectionStart.getTime(), sectionEnd.getTime());
            //log.trace("Total m2kW: " + eds.stream().map(ElectricData::getM2kW).reduce(BigDecimal.ZERO, BigDecimal::add));
            eds.stream().forEach(ed -> {
                BigDecimal touPrice = useTou.currentPriceOfTime(ed.getTime(), Price_Ratio_Peak, Price_Ratio_OffPeak);
                if (ed.getM3kW().compareTo(BigDecimal.ZERO) < 0) { // 充電
                    ed.setM3kW(ed.getM3kW().multiply(ESS_Effi).add(ESS_Self_kWh.multiply(Utility.kwhTokW)));
                } else {
                    ed.setM3kW(ed.getM3kW().add(ESS_Self_kWh.multiply(Utility.kwhTokW)));
                }
                /**
                 * if (T.M3 < 0) //充電 T.M3 = (T.M3 * ESS_Effi) + ESS_Self_kWh; //充電需補償轉換效率 else
                 * T.M3 = T.M3 + ESS_Self_kWh;
                 */
                ed.balance();
                BigDecimal NormalDay_Peak_A_KW = ed.getM5kW().add(ed.getM6kW()).add(ed.getM7kW()).subtract(ed.getM3kW())
                                                   .subtract(ed.getM2kW());
                BigDecimal NormalDay_Peak_A2_KW = ed.getM5kW().add(ed.getM6kW()).add(ed.getM7kW())
                                                    .subtract(ed.getM3kW());
                BigDecimal NormalDay_Peak_B_KW = ed.getM5kW().add(ed.getM6kW()).add(ed.getM7kW());
                BigDecimal NormalDay_Peak_M1_KW = ed.getM1kW();
                BigDecimal NormalDay_Peak_M2_KW = ed.getM2kW();
                BigDecimal NormalDay_Peak_M3_KW = ed.getM3kW();
                BigDecimal NormalDay_Peak_M7_KW = ed.getM7kW();
                BigDecimal NormalDay_Peak_M2_M3_M7_KW = ed.getM2kW().add(ed.getM3kW()).add(ed.getM7kW());

                touFee.Total_Bill_A_KWH = touFee.Total_Bill_A_KWH
                        .add(NormalDay_Peak_A_KW.multiply(touPrice).multiply(Utility.toKwh));
                touFee.Total_Bill_A2_KWH = touFee.Total_Bill_A2_KWH
                        .add(NormalDay_Peak_A2_KW.multiply(touPrice).multiply(Utility.toKwh));
                touFee.Total_Bill_B_KWH = touFee.Total_Bill_B_KWH
                        .add(NormalDay_Peak_B_KW.multiply(touPrice).multiply(Utility.toKwh));
                touFee.Total_Bill_M1_KWH = touFee.Total_Bill_M1_KWH
                        .add(NormalDay_Peak_M1_KW.multiply(touPrice).multiply(Utility.toKwh));
                //log.trace("{}+{}({}*{}*{})",touFee.Total_Bill_M2_KWH,NormalDay_Peak_M2_KW.multiply(touPrice).multiply(Utility.toKwh), NormalDay_Peak_M2_KW,touPrice,Utility.toKwh);
                touFee.Total_Bill_M2_KWH = touFee.Total_Bill_M2_KWH
                        .add(NormalDay_Peak_M2_KW.multiply(touPrice).multiply(Utility.toKwh));
                touFee.Total_Bill_M3_KWH = touFee.Total_Bill_M3_KWH
                        .add(NormalDay_Peak_M3_KW.multiply(touPrice).multiply(Utility.toKwh));
                touFee.Total_Bill_M7_KWH = touFee.Total_Bill_M7_KWH
                        .add(NormalDay_Peak_M7_KW.multiply(touPrice).multiply(Utility.toKwh));
                touFee.Total_Bill_M2_M3_M7_KWH = touFee.Total_Bill_M2_M3_M7_KWH
                        .add(NormalDay_Peak_M2_M3_M7_KW.multiply(touPrice).multiply(Utility.toKwh));

            });

        }
        return touFee;
    }

    @Override
    public BouFee BOU_PF_Bill(Date Start_Day, Date End_Day, long Profile_id, int datatype, double Price_Ratio_BOU,
            double Price_Ratio_Peak, double Price_Ratio_OffPeak, double ESS_Self_kWh, double ESS_Effi) {

        return BOU_PF_Bill(Start_Day, End_Day, Profile_id, DataType.getCode(datatype),
                BigDecimal.valueOf(Price_Ratio_BOU), BigDecimal.valueOf(Price_Ratio_Peak),
                BigDecimal.valueOf(Price_Ratio_OffPeak), BigDecimal.valueOf(ESS_Self_kWh),
                BigDecimal.valueOf(ESS_Effi));
    }

    @Override
    public BouFee BOU_PF_Bill(Date Start_Day, Date End_Day, long Profile_id, DataType datatype,
            BigDecimal Price_Ratio_BOU, BigDecimal Price_Ratio_Peak, BigDecimal Price_Ratio_OffPeak,
            BigDecimal ESS_Self_kWh, BigDecimal ESS_Effi) {
        Check(Start_Day, End_Day);
        BouFee fee = new BouFee();
        Optional<FieldProfile> ofield = fieldProfileRepository.findById(Profile_id);
        if (!ofield.isPresent()) {
            throw new IiiException("查無場域資訊");
        }
        FieldProfile fieldProfile = ofield.get();
        BigDecimal tyodc = BigDecimal.valueOf(fieldProfile.getTyodc());
        BigDecimal oyod = BigDecimal.valueOf(fieldProfile.getOyod());
        Calendar startDay = Utility.GetControlDay(Start_Day);
        Calendar endDay = Utility.GetControlDay(End_Day);
        endDay.add(Calendar.DAY_OF_YEAR, 1);

        List<TouOfTPH3S> tous = touOfTPH3SRepository.findAll();

        Calendar walkMonth = Utility.GetControlMonth1st(Start_Day);
        while (walkMonth.compareTo(endDay) < 0) {

            Calendar month1st = (Calendar) walkMonth.clone();

            walkMonth.add(Calendar.MONTH, 1);
            Calendar nextmonth = (Calendar) walkMonth.clone();

            // 依照日期區間決定使用電價版本
            Optional<TouOfTPH3S> ouseTou = tous.stream()
                                               .filter(a -> a.getActiveTime().compareTo(month1st.getTime()) <= 0)
                                               .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
            if (!ouseTou.isPresent()) {
                throw new IiiException("查無對應區間電價");
            }

            TouOfTPH3S useTou = ouseTou.get();
            // 決定用電資料計算區間
            Calendar sectionStart = startDay.compareTo(month1st) >= 0 ? startDay : month1st;
            Calendar sectionEnd = nextmonth.compareTo(endDay) < 0 ? nextmonth : endDay;
            Calendar touStart = sectionStart;
            Calendar touEnd = (Calendar) sectionEnd.clone();
            touEnd.add(Calendar.DAY_OF_YEAR, -1);
            long daysInMonth = ChronoUnit.DAYS.between(month1st.toInstant(), nextmonth.toInstant());
            long actualDays = ChronoUnit.DAYS.between(sectionStart.toInstant(), sectionEnd.toInstant());
            BigDecimal monthRate = BigDecimal.valueOf(actualDays).divide(BigDecimal.valueOf(daysInMonth), 3,
                    BigDecimal.ROUND_HALF_UP);

            BigDecimal Regular_Contract_Cost = BigDecimal.ZERO;
            if (useTou.isSummerMonth(month1st)) {
                Regular_Contract_Cost = useTou.getSummer_Regular_Contarct().multiply(Price_Ratio_BOU);
            } else {
                Regular_Contract_Cost = useTou.getNonSummer_Regular_Contarct().multiply(Price_Ratio_BOU);
            }
            BigDecimal PF_Reduce_Percent = useTou.getPF_Adj();

            TouFee touFee = TimeOfUsed_Bill(touStart.getTime(), touEnd.getTime(), Profile_id, datatype,
                    Price_Ratio_Peak, Price_Ratio_OffPeak, ESS_Self_kWh, ESS_Effi);

            try {
                // ===============================M1=======================================//
                {
                    BouPartFee m1Part = GetM1Bou(sectionStart.getTime(), sectionEnd.getTime(), Profile_id, datatype,
                            touFee, tyodc, Regular_Contract_Cost, monthRate, PF_Reduce_Percent);
                    fee.BOU_Bill_M1 = fee.BOU_Bill_M1.add(m1Part.BOU_Bill);
                    fee.PF_Bill_M1 = fee.PF_Bill_M1.add(m1Part.PF_Bill);
                }
                // ===============================M1_M7=======================================//
                {
                    BouPartFee m1Part = GetM1_M7Bou(sectionStart.getTime(), sectionEnd.getTime(), Profile_id, datatype,
                            touFee, tyodc, Regular_Contract_Cost, monthRate, PF_Reduce_Percent);
                    fee.BOU_Bill_M1_M7 = fee.BOU_Bill_M1_M7.add(m1Part.BOU_Bill);
                    fee.PF_Bill_M1_M7 = fee.PF_Bill_M1_M7.add(m1Part.PF_Bill);
                }
                // ===============================M1_M2_M3_M7=======================================//
                {
                    // 計算契約容量是用OYOD，要特別注意，這樣費用才會比較高
                    BouPartFee m1Part = GetM1_M2_M3_M7Bou(sectionStart.getTime(), sectionEnd.getTime(), Profile_id,
                            datatype, touFee, oyod, Regular_Contract_Cost, monthRate, PF_Reduce_Percent);
                    fee.BOU_Bill_M1_M2_M3_M7 = fee.BOU_Bill_M1_M2_M3_M7.add(m1Part.BOU_Bill);
                    fee.PF_Bill_M1_M2_M3_M7 = fee.PF_Bill_M1_M2_M3_M7.add(m1Part.PF_Bill);
                }
            } catch (Throwable ex) {
                log.error(String.format("%s from: %s  until: %s ", Profile_id, sectionStart.getTime(),
                        sectionEnd.getTime()));
                throw ex;
            }
        }

        return fee;
    }
}

@ToString
class BouPartFee {
    BigDecimal Regular_Contrat_Bill = BigDecimal.ZERO;
    BigDecimal Over_Contract_Bill = BigDecimal.ZERO;
    BigDecimal BOU_Bill = BigDecimal.ZERO;
    BigDecimal PF_Bill = BigDecimal.ZERO;
}
