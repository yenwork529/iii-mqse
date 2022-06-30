package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

public class TxgBidDetail {


//    /**
//     * SP即時備轉容量輔助服務
//     */
//    @DBRef
//    @Field("srId")
//    @JsonIgnore
//    private SpinReserveProfile id;
//    /**
//     * 所屬場域
//     */
//    @DBRef
//    @Field("fieldId")
//    //	@JsonIgnore
//    @JsonIgnoreProperties({"updateTime", "createTime", "policyProfile", "companyProfile",
//            "automaticFrequencyControlProfile", "demandResponseProfile", "spinReserveProfile",
//            "srIndex", "touType", "tyod", "tyodc", "trhd", "oyod", "isReserve", "targetType",
//            "frequency", "delay", "stationId", "tcEnable", "tcIp", "tcLastUploadTime",
//            "devStatus", "forecastFixTime", "rescheduleTime", "unload", "isSync",
//            "isNeedReschedule","PPACapacity","serviceFactor","efficacyRevenu","energyRevenue","serviceEnergy"})
//    private FieldProfile fieldProfile;

    /**
     *
     */
//    @DBRef
//    @Field("resId")
//    //	@JsonIgnore
//    @JsonIgnoreProperties({"updateTime", "createTime", "policyProfile", "companyProfile",
//            "automaticFrequencyControlProfile", "demandResponseProfile", "spinReserveProfile",
//            "srIndex", "touType", "tyod", "tyodc", "trhd", "oyod", "isReserve", "targetType",
//            "frequency", "delay", "stationId", "tcEnable", "tcIp", "tcLastUploadTime",
//            "devStatus", "forecastFixTime", "rescheduleTime", "unload", "isSync",
//            "isNeedReschedule","PPACapacity","serviceFactor","efficacyRevenu","energyRevenue","serviceEnergy"})
//    private TxgFieldProfile txgFieldProfile;
    private String resId;

    /**
     * SP即時備轉參與日期時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    //	@JsonIgnore
    private Date timestamp;

//    /**
//     * 資料更新時間
//     */
//    @LastModifiedDate
//    @JsonIgnore
//    private Date updateTime;
    /**
     * 資料建立時間
     */
    @JsonIgnore
    private Date createTime = new Date();
    /**
     * 可調度容量(MW)
     */
    private BigDecimal sr_capacity;
    /**
     * 得標容量(MW)
     */
    private BigDecimal awarded_capacity;
    /**
     * 容量費用
     */
    private BigDecimal capacityRevenue;

    /**
     * 8/23
     * SR/SUP新增欄位
     */

    /**
     * 尖峰保證容量(MW)
     */
    private BigDecimal PPACapacity;

    /**
     * 服務品質指標(%)
     */
    private BigDecimal serviceFactor;

    /**
     * 效能費 (NT)
     */
    private BigDecimal efficacyRevenue;

    /**
     * 電能費
     */
    private BigDecimal energyRevenue;

    /**
     * 提供電能量(MWh-小時or天)
     */
    private BigDecimal serviceEnergy;
}
