package org.iii.esd.bill;

import java.math.BigDecimal;
import java.util.Date;

import org.iii.esd.enums.DataType;
import org.iii.esd.exception.IiiException;

public interface IBillService {

    /**
     * 計算流動電費TOU，假如是要計算2017/01/01到2017年底，這邊的起始日跟結束日是看當天日期<br>
     * 所以是2017/01/01 - 2017/12/31，假如傳成2017/01/01 - 2018/01/01會多算一天，請務必注意
     *
     * @param Start_Day           開始日
     * @param End_Day             結束日
     * @param Profile_id          Profile id
     * @param Data_Type           使用何種資料類型
     * @param Price_Ratio_Peak    尖峰,半尖峰倍率
     * @param Price_Ratio_OffPeak 離峰倍率
     * @param ESS_Self_kWh        ESS自放度數
     * @param ESS_Effi            ESS補償轉換效率
     * @return
     */
    public TouFee TimeOfUsed_Bill(Date Start_Day, Date End_Day, long Profile_id, int Data_Type, double Price_Ratio_Peak,
            double Price_Ratio_OffPeak, double ESS_Self_kWh, double ESS_Effi);

    /**
     * 計算流動電費TOU，假如是要計算2017/01/01到2017年底，這邊的起始日跟結束日是看當天日期<br>
     * 所以是2017/01/01 - 2017/12/31，假如傳成2017/01/01 - 2018/01/01會多算一天，請務必注意
     *
     * @param Start_Day           開始日
     * @param End_Day             結束日
     * @param Profile_id          Profile id
     * @param Data_Type           使用何種資料類型
     * @param Price_Ratio_Peak    尖峰,半尖峰倍率
     * @param Price_Ratio_OffPeak 離峰倍率
     * @param ESS_Self_kWh        ESS自放度數
     * @param ESS_Effi            ESS補償轉換效率
     * @return
     */
    public TouFee TimeOfUsed_Bill(Date Start_Day, Date End_Day, long Profile_id, DataType Data_Type,
            BigDecimal Price_Ratio_Peak, BigDecimal Price_Ratio_OffPeak, BigDecimal ESS_Self_kWh, BigDecimal ESS_Effi);

    /**
     * 基本電費(含PF調整費)
     *
     * @param Start_Day           開始日
     * @param End_Day             結束日
     * @param Profile_id          Profile id
     * @param DataType            使用何種資料類型
     * @param Price_Ratio_BOU     基本電費倍率
     * @param Price_Ratio_Peak    尖峰,半尖峰倍率
     * @param Price_Ratio_OffPeak 離峰倍率
     * @param ESS_Self_kWh        ESS自放度數
     * @param ESS_Effi            ESS補償轉換效率
     * @return
     */
    public BouFee BOU_PF_Bill(Date Start_Day, Date End_Day, long Profile_id, int DataType, double Price_Ratio_BOU,
            double Price_Ratio_Peak, double Price_Ratio_OffPeak, double ESS_Self_kWh, double ESS_Effi);

    /**
     * 基本電費(含PF調整費)
     *
     * @param Start_Day           開始日
     * @param End_Day             結束日
     * @param Profile_id          Profile id
     * @param DataType            使用何種資料類型
     * @param Price_Ratio_BOU     基本電費倍率
     * @param Price_Ratio_Peak    尖峰,半尖峰倍率
     * @param Price_Ratio_OffPeak 離峰倍率
     * @param ESS_Self_kWh        ESS自放度數
     * @param ESS_Effi            ESS補償轉換效率
     * @return
     */
    public BouFee BOU_PF_Bill(Date Start_Day, Date End_Day, long Profile_id, DataType datatype, BigDecimal Price_Ratio_BOU,
            BigDecimal Price_Ratio_Peak, BigDecimal Price_Ratio_OffPeak, BigDecimal ESS_Self_kWh, BigDecimal ESS_Effi);

    public default void Check(Date start, Date end) {
        if (start.compareTo(end) > 0) {
            throw new IiiException("效益計算區間有誤");
        }
    }

}