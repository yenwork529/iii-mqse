package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Deprecated
public class SpinReserveDataDetail {

    @DBRef
    @Field("fieldId")
    @JsonIgnoreProperties({"updateTime", "createTime", "policyProfile", "companyProfile",
            "automaticFrequencyControlProfile", "demandResponseProfile", "spinReserveProfile",
            "srIndex", "touType", "tyod", "tyodc", "trhd", "oyod", "isReserve", "targetType",
            "frequency", "delay", "stationId", "tcEnable", "tcIp", "tcLastUploadTime",
            "devStatus", "forecastFixTime", "rescheduleTime", "unload", "isSync",
            "isNeedReschedule","clipKW","serviceEnergy","performanceEnergy"})
    private FieldProfile fieldProfile;
    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;
    /**
     * 資料建立時間
     */
    @JsonIgnore
    private Date createTime;

    /**
     * 基準線(通知前5分鐘平均需量)
     */
    private BigDecimal baseline;

    /**
     * 執行每分鐘平均降載容量
     */
    private BigDecimal clippedKW;

    /**
     * 當次執行電能費用
     */
    private BigDecimal revenuePrice;

    /**
     * 8/23
     * SR/SUP新增欄位
     */

    /**
     * 場域降載量(kWh)
     */  
    private BigDecimal clipKW;
    
    /**
     * 服務提供之電能量(MWh)
     */  
    private BigDecimal serviceEnergy;
    
    /**
     * 執行率期間提供之電能量(MWh)
     */
    private BigDecimal performanceEnergy;
    
    /**
     *  場域執行率(%)
     */
    private BigDecimal revenueFactor;;


    /**
     * 降載量
     */
    private BigDecimal target;
}