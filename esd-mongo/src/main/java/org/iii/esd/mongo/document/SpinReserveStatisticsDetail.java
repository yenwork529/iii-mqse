package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
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
@ToString
public class SpinReserveStatisticsDetail {

    @DBRef
    @Field("fieldId")
    @JsonIgnoreProperties({"updateTime", "createTime", "policyProfile", "companyProfile",
            "automaticFrequencyControlProfile", "demandResponseProfile", "spinReserveProfile",
            "srIndex", "touType", "tyod", "tyodc", "trhd", "oyod", "isReserve", "targetType",
            "frequency", "delay", "stationId", "tcEnable", "tcIp", "tcLastUploadTime",
            "devStatus", "forecastFixTime", "rescheduleTime", "unload", "isSync",
            "isNeedReschedule", "efficacyRevenue", "energyRevenue", "avgCapacityPrice", "totalAwardedCapacity"})
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
     * 容量費用
     */
    private BigDecimal capacityRevenue;
    /**
     * 電能費用
     */
    private BigDecimal energyPrice;
    /**
     * 平均執行率
     */
    private BigDecimal avgRevenueFactor;
    /**
     * 執行降載次數
     */
    private Integer noticeConut;
    /**
     * 得標時數
     */
    private Integer awardedCount;

    /**
     * 8/23
     * SR/SUP新增欄位
     */

    /**
     * 效能費
     */
    private BigDecimal efficacyRevenue;

    /**
     * 新版電能費
     */
    private BigDecimal energyRevenue;

    /**
     * 平均容量報價 //Sam 20201103
     */
    private BigDecimal avgCapacityPrice;
    /**
     * 總得標容量 //Sam 20201103
     */
    private BigDecimal totalAwardedCapacity;

}