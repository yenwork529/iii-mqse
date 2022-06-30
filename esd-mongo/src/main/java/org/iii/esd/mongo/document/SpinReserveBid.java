package org.iii.esd.mongo.document;

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
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.OperatorStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "SpinReserveBid")
@CompoundIndexes({
        @CompoundIndex(def = "{'srId':1, 'timestamp':1}",
                name = "pk_spinReserveBid",
                unique = true)
})
@Deprecated
public class SpinReserveBid extends UuidDocument {

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
     * SP即時備轉參與日期時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
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
     * 可調度容量(MW)
     */
    private BigDecimal sr_capacity;
    /**
     * 容量費單價(NT/MW)
     */
    private BigDecimal sr_price;
    /**
     * 電能費報價(NT$/MWh)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal energyPrice;
    /**
     * 尖峰保證容量電能費價格(NT$/MWh)
     */
//     @JsonInclude(JsonInclude.Include.NON_NULL)
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
    private List<SpinReserveBidDetail> list;


    /**
     * 8/23
     * SR/SUP新增欄位
     */

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

    public static SpinReserveBid makeBid(SpinReserveProfile sp, FieldProfile fp, Long ms){
            ms = (ms / 60*60*1000L) * 60*60*1000L;
            SpinReserveBid bid = new SpinReserveBid();
            bid.setSpinReserveProfile(sp);
            bid.setTimestamp(new Date(ms));
            return bid;
    }
}