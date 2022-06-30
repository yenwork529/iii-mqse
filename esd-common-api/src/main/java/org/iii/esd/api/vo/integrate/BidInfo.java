package org.iii.esd.api.vo.integrate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.OperatorStatus;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BidInfo {

    /**
     * dReg AS
     */
    @DBRef
    @Field("afcId")
    @JsonIgnore
    private AutomaticFrequencyControlProfile afcProfile;

    /**
     * SP即時備轉容量輔助服務
     */
    @DBRef
    @Field("srId")
    //	@JsonIgnore
    @JsonIgnoreProperties({"updateTime", "createTime", "dnpURL", "enableStatus",
            "bidContractCapacity", "noticeTime", "startTime", "endTime", "clipKW"})
    private SpinReserveProfile spinReserveProfile;

    /**
     * 交易群組 ID
     */
    private String txgId;

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
    private Date createTime = new Date();

    /**
     * SP即時備轉參與日期時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    /**
     * 可調度容量(MW)
     */
    private BigDecimal sr_capacity;

    /**
     * 可調度容量(MW)
     */
    private BigDecimal capacity;

    /**
     * 容量費單價(NT/MW)
     */
    private BigDecimal sr_price;

    /**
     * 容量費單價(NT/MW)
     */
    private BigDecimal price;

    /**
     * 電能費報價(NT$/MWh)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal energyPrice;

    private BigDecimal dreg;

    private BigDecimal sr;

    private BigDecimal sup;

    /**
     * 尖峰保證容量電能費價格(NT$/MWh)
     */
    private BigDecimal ppaEnergyPrice;

    /**
     * 得標容量(MW)
     */
    private BigDecimal awarded_capacity;

    /**
     * 尖峰保證容量(MW)
     */
    private BigDecimal ppa_capacity;

    /**
     * 運轉狀態
     */
    private OperatorStatus operatorStatus;

    /**
     * 容量費用
     */
    private BigDecimal capacityRevenue;

    /**
     * 競標容量報價明細
     */
    private List<BidDetail> list;

    /**
     * 服務品質指標(%)
     */
    private BigDecimal serviceFactor;

    /**
     * 得標執行結果
     */
    private BigDecimal awardedFactor;

    /**
     * 效能費 (NT)
     */
    private BigDecimal efficacyRevenue;

    /**
     * 電能費
     */
    private BigDecimal energyRevenue;

    /**
     * 提供電能量(kWh-小時)
     */
    private BigDecimal serviceEnergy;

    /**
     * 中止待命
     */
    private Boolean abandon;

    /**
     * 中止待命開始時間
     */
    private Date abandonFrom;

    public void cleanNull() {
        if (this.capacity == null) {
            this.capacity = BigDecimal.ZERO;
        }

        if (this.awarded_capacity == null) {
            this.awarded_capacity = BigDecimal.ZERO;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class BidDetail {
        /**
         * SP即時備轉容量輔助服務
         */
        @DBRef
        @Field("srId")
        @JsonIgnore
        private SpinReserveProfile id;

        /**
         * 所屬場域
         */
        @DBRef
        @Field("fieldId")
        //	@JsonIgnore
        @JsonIgnoreProperties({"updateTime", "createTime", "policyProfile", "companyProfile",
                "automaticFrequencyControlProfile", "demandResponseProfile", "spinReserveProfile",
                "srIndex", "touType", "tyod", "tyodc", "trhd", "oyod", "isReserve", "targetType",
                "frequency", "delay", "stationId", "tcEnable", "tcIp", "tcLastUploadTime",
                "devStatus", "forecastFixTime", "rescheduleTime", "unload", "isSync",
                "isNeedReschedule", "PPACapacity", "serviceFactor", "efficacyRevenu", "energyRevenue", "serviceEnergy"})
        private FieldProfile fieldProfile;

        /**
         * 資源 ID
         */
        private String resId;

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
        private Date createTime = new Date();

        /**
         * SP即時備轉參與日期時間
         */
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        private Date timestamp;

        /**
         * 可調度容量(MW)
         */
        private BigDecimal sr_capacity;

        /**
         * 可調度容量(MW)
         */
        private BigDecimal capacity;

        /**
         * 得標容量(MW)
         */
        private BigDecimal awarded_capacity;

        /**
         * 容量費用
         */
        private BigDecimal capacityRevenue;

        /**
         * 尖峰保證容量(MW)
         */
        private BigDecimal ppa_capacity;

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

        public void cleanNull() {
            if (this.capacity == null) {
                this.capacity = BigDecimal.ZERO;
            }

            if (this.awarded_capacity == null) {
                this.awarded_capacity = BigDecimal.ZERO;
            }
        }

    }
}
