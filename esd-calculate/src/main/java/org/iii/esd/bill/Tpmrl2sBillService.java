package org.iii.esd.bill;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.caculate.Utility;
import org.iii.esd.enums.DataType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.AbstractTou;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.TouOfTPMRL2S;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.repository.TouOfTPMRL2SRepository;

@Log4j2
@Service
public class Tpmrl2sBillService implements IBillService {
    final BigDecimal limit = BigDecimal.valueOf(2000);
    @Autowired
    TouOfTPMRL2SRepository touOfTPMRL2SRepository;
    @Autowired
    ElectricDataRepository electricDataRepository;

    /**
     * 有一個大BUG，在超過2000度，因為罰則的關係，計算金額要拆帳的時候會有問題
     * 不知道怎麼拆，
     */
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

        TouFee totalTouFee = new TouFee();
        Calendar startDay = Utility.GetControlDay(Start_Day);
        Calendar endDay = Utility.GetControlDay(End_Day);
        endDay.add(Calendar.DAY_OF_YEAR, 1);

        List<TouOfTPMRL2S> tous = touOfTPMRL2SRepository.findAll();

        Calendar walkMonth = Utility.GetControlMonth1st(Start_Day);
        while (walkMonth.compareTo(endDay) < 0) {
            TouFee monthTouFee = new TouFee();
            Calendar month1st = (Calendar) walkMonth.clone();

            walkMonth.add(Calendar.MONTH, 1);
            Calendar nextmonth = (Calendar) walkMonth.clone();

            // 依照日期區間決定使用電價版本
            Optional<TouOfTPMRL2S> ouseTou = tous.stream()
                                                 .filter(a -> a.getActiveTime().compareTo(month1st.getTime()) <= 0)
                                                 .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
            if (!ouseTou.isPresent()) {
                throw new IiiException("查無對應區間電價");
            }
            TouOfTPMRL2S useTou = ouseTou.get();

            // 決定用電資料計算區間
            Calendar sectionStart = startDay.compareTo(month1st) >= 0 ? startDay : month1st;
            Calendar sectionEnd = nextmonth.compareTo(endDay) < 0 ? nextmonth : endDay;
            List<ElectricData> eds = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(Profile_id, Data_Type,
                    sectionStart.getTime(), sectionEnd.getTime());
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

                monthTouFee.Total_Bill_A_KWH = monthTouFee.Total_Bill_A_KWH
                        .add(NormalDay_Peak_A_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_A2_KWH = monthTouFee.Total_Bill_A2_KWH
                        .add(NormalDay_Peak_A2_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_B_KWH = monthTouFee.Total_Bill_B_KWH
                        .add(NormalDay_Peak_B_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_M1_KWH = monthTouFee.Total_Bill_M1_KWH
                        .add(NormalDay_Peak_M1_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_M2_KWH = monthTouFee.Total_Bill_M2_KWH
                        .add(NormalDay_Peak_M2_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_M3_KWH = monthTouFee.Total_Bill_M3_KWH
                        .add(NormalDay_Peak_M3_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_M7_KWH = monthTouFee.Total_Bill_M7_KWH
                        .add(NormalDay_Peak_M7_KW.multiply(touPrice).multiply(Utility.toKwh));
                monthTouFee.Total_Bill_M2_M3_M7_KWH = monthTouFee.Total_Bill_M2_M3_M7_KWH
                        .add(NormalDay_Peak_M2_M3_M7_KW.multiply(touPrice).multiply(Utility.toKwh));

            });
            log.trace("罰則前:" + monthTouFee);
            // 每個月結束還要檢查有沒有超過用電量
            // 假如超過月使用超過2000度，超過2000度的部分要額外支出費用
            {
                BigDecimal NormalDay_Peak_A_KWH = eds.stream().map(a -> {
                    return a.getM5kW().add(a.getM6kW()).add(a.getM7kW()).subtract(a.getM3kW()).subtract(a.getM2kW());
                }).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
                if (NormalDay_Peak_A_KWH.compareTo(BigDecimal.ZERO) > 0) {
                    monthTouFee.Total_Bill_A_KWH = monthTouFee.Total_Bill_A_KWH
                            .add(NormalDay_Peak_A_KWH.multiply(useTou.getOvercost()));
                }

            }
            {
                BigDecimal NormalDay_Peak_A2_KWH = eds.stream().map(a -> {
                    return a.getM5kW().add(a.getM6kW()).add(a.getM7kW()).subtract(a.getM3kW());
                }).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
                if (NormalDay_Peak_A2_KWH.compareTo(BigDecimal.ZERO) > 0) {
                    monthTouFee.Total_Bill_A2_KWH = monthTouFee.Total_Bill_A2_KWH
                            .add(NormalDay_Peak_A2_KWH.multiply(useTou.getOvercost()));
                }

            }
            {
                BigDecimal NormalDay_Peak_B_KWH = eds.stream().map(a -> {
                    return a.getM5kW().add(a.getM6kW()).add(a.getM7kW());
                }).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
                if (NormalDay_Peak_B_KWH.compareTo(BigDecimal.ZERO) > 0) {
                    monthTouFee.Total_Bill_B_KWH = monthTouFee.Total_Bill_B_KWH
                            .add(NormalDay_Peak_B_KWH.multiply(useTou.getOvercost()));
                }

            }
            {

                BigDecimal NormalDay_Peak_M1_KWH = eds.stream().map(a -> {
                    return a.getM1kW();
                }).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
                if (NormalDay_Peak_M1_KWH.compareTo(BigDecimal.ZERO) > 0) {
                    monthTouFee.Total_Bill_M1_KWH = monthTouFee.Total_Bill_M1_KWH
                            .add(NormalDay_Peak_M1_KWH.multiply(useTou.getOvercost()));
                }
            }
            // 單項貢獻不計算罰則
			/*
			{
				BigDecimal NormalDay_Peak_M2_KWH = eds.stream().map(a -> {
					return a.getM2kW();
				}).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
				if (NormalDay_Peak_M2_KWH.compareTo(BigDecimal.ZERO) > 0) {
					monthTouFee.Total_Bill_M2_KWH = monthTouFee.Total_Bill_M2_KWH
							.add(NormalDay_Peak_M2_KWH.multiply(useTou.getOvercost()));
				}

			}

			{
				BigDecimal NormalDay_Peak_M3_KWH = eds.stream().map(a -> {
					return a.getM3kW();
				}).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
				if (NormalDay_Peak_M3_KWH.compareTo(BigDecimal.ZERO) > 0) {
					monthTouFee.Total_Bill_M3_KWH = monthTouFee.Total_Bill_M3_KWH
							.add(NormalDay_Peak_M3_KWH.multiply(useTou.getOvercost()));
				}

			}
			{
				BigDecimal NormalDay_Peak_M7_KWH = eds.stream().map(a -> {
					return a.getM7kW();
				}).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
				if (NormalDay_Peak_M7_KWH.compareTo(BigDecimal.ZERO) > 0) {
					monthTouFee.Total_Bill_M7_KWH = monthTouFee.Total_Bill_M7_KWH
							.add(NormalDay_Peak_M7_KWH.multiply(useTou.getOvercost()));
				}

			}
			{
				BigDecimal NormalDay_Peak_M2_M3_M7_KWH = eds.stream().map(a -> {
					return a.getM7kW();
				}).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(Utility.toKwh).subtract(limit);
				if (NormalDay_Peak_M2_M3_M7_KWH.compareTo(BigDecimal.ZERO) > 0) {
					monthTouFee.Total_Bill_M2_M3_M7_KWH = monthTouFee.Total_Bill_M2_M3_M7_KWH
							.add(NormalDay_Peak_M2_M3_M7_KWH.multiply(useTou.getOvercost()));
				}

			}
			*/
            log.trace("罰則後:" + monthTouFee);
            totalTouFee.AddFee(monthTouFee);
        }

        return totalTouFee;
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
        Calendar startDay = Utility.GetControlDay(Start_Day);
        Calendar endDay = Utility.GetControlDay(End_Day);
        endDay.add(Calendar.DAY_OF_YEAR, 1);

        List<TouOfTPMRL2S> tous = touOfTPMRL2SRepository.findAll();

        Calendar walkMonth = Utility.GetControlMonth1st(Start_Day);
        while (walkMonth.compareTo(endDay) < 0) {

            Calendar month1st = (Calendar) walkMonth.clone();

            walkMonth.add(Calendar.MONTH, 1);
            Calendar nextmonth = (Calendar) walkMonth.clone();

            // 依照日期區間決定使用電價版本
            Optional<TouOfTPMRL2S> ouseTou = tous.stream()
                                                 .filter(a -> a.getActiveTime().compareTo(month1st.getTime()) <= 0)
                                                 .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
            if (!ouseTou.isPresent()) {
                throw new IiiException("查無對應區間電價");
            }

            TouOfTPMRL2S useTou = ouseTou.get();
            // 決定用電資料計算區間
            Calendar sectionStart = startDay.compareTo(month1st) >= 0 ? startDay : month1st;
            Calendar sectionEnd = nextmonth.compareTo(endDay) < 0 ? nextmonth : endDay;
            long daysInMonth = ChronoUnit.DAYS.between(month1st.toInstant(), nextmonth.toInstant());
            long actualDays = ChronoUnit.DAYS.between(sectionStart.toInstant(), sectionEnd.toInstant());
            BigDecimal monthRate = BigDecimal.valueOf(actualDays).divide(BigDecimal.valueOf(daysInMonth), 3,
                    BigDecimal.ROUND_HALF_UP);
            fee.BOU_Bill_M1 = fee.BOU_Bill_M1.add(useTou.getBasic().multiply(monthRate));
        }
        fee.BOU_Bill_M1_M7 = fee.BOU_Bill_M1;
        fee.BOU_Bill_M1_M2_M7 = fee.BOU_Bill_M1;
        fee.BOU_Bill_M1_M2_M3_M7 = fee.BOU_Bill_M1;
        return fee;
    }
}
