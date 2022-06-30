package org.iii.esd.mongo.document.integrate;
/*
SpinReserveBid
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.iii.esd.enums.OperatorStatus;
//import org.iii.esd.mongo.document.TXGBidDetail;
//import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgBid")
@CompoundIndexes(
        {
                @CompoundIndex(
                        def = "{'txgId':1 , 'timestamp':1}",
                        name = "pk_TxgBid",
                        unique = true
                ),
        }
)

public class TxgBid extends UuidDocument {

    /**
     * dReg AS
     */
//    @DBRef
//    @Field("TXGafcId")
//    @JsonIgnore
//    private TxgAfcProfile txgafcProfile;
//
//    /**
//     * SP即時備轉容量輔助服務
//     */
//    @DBRef
//    @Field("srId")
//    //	@JsonIgnore
//    @JsonIgnoreProperties({"updateTime", "createTime", "dnpURL", "enableStatus",
//            "bidContractCapacity", "noticeTime", "startTime", "endTime", "clipKW"})
//    private SpinReserveProfile spinReserveProfile;

    /**
     * txgID綁定SP即時備轉容量輔助服務(TXG)
     */
//    @DBRef
//    @Field("txgId")
//    @JsonIgnoreProperties({"updateTime", "createTime", "dnpURL", "enableStatus",
//            "bidContractCapacity", "noticeTime", "startTime", "endTime", "clipKW"})
//    private TxgProfile TXGProfile;

    private String txgId;

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
    private List<TxgBidDetail> list;


    /**
     * 8/23
     * SR/SUP新增欄位
     * <p>
     * /**
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


}
