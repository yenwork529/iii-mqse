package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgData")
@CompoundIndexes(
        {
                @CompoundIndex(
                        def = "{'txgId':1 , 'noticeType':1, 'noticeTime':1}",
                        name = "pk_TxgData",
                        unique = true
                ),
                @CompoundIndex(def = "{'txgId':1}", name = "ix_TxgData1"),
        }
)
public class TxgData extends UuidDocument {


//    /**
//     * SP即時備轉容量輔助服務
//     */
//    @DBRef
//    @Field("srId")
//    private SpinReserveProfile spinReserveProfile;

    /**
     * SP即時備轉容量輔助服務(txg)
     */
//    @DBRef
//    @Field("txgId")
//    private TxgProfile TXGProfile;

    private String txgId;

    /**
     * 通知類型
     */
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    /**
     * 通知時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date noticeTime;

    /**
     * 開始時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date startTime;

    /**
     * 結束時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date endTime;

    /**
     * 卸載量(得標量)
     */
    private Integer clipKW;

    /**
     * 基準線(通知前5分鐘平均需量)
     */
    private BigDecimal baseline;

    /**
     * 電能費(元/ kWh)
     */
    private BigDecimal energyPrice;

    /**
     * 執行每分鐘平均降載容量
     */
    private BigDecimal clippedKW;

    /**
     * 每分鐘平均執行率(%)
     */
    private BigDecimal revenueFactor;

    /**
     * 當次執行電能費用
     */
    private BigDecimal revenuePrice;

    /**
     * 場域執行資料
     */
    private List<TxgDataDetail> list;

    /**
     * M1到t2的累積用電量(KWH)
     */
//  private BigDecimal powerM1t2;

    /**
     * CBL到t2的累積用電量(KWH)(2021/08/22取消設計)
     */
//  private BigDecimal powerCBLt2;

    /**
     * CBL到t3的累積用電量(KWH)
     */
//  private BigDecimal powerCBLt3;

    /**
     * M1到T3的累積用電量(KWH)
     */
//  private BigDecimal energyT3;

    /**
     * M1到T5的累積用電量(KWH)
     */
//  private BigDecimal energyT5;

    /**
     * 8/23
     * SR/SUP新增欄位
     */

    /**
     * 服務提供之電能量(kWh)
     */
    private BigDecimal serviceEnergy;

    /**
     * 執行率期間提供之電能量(kWh)
     */

    private BigDecimal performanceEnergy;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;
}
