package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

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

import org.iii.esd.enums.StatisticsType;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "SpinReserveStatisticsLegacy")
@CompoundIndexes({
        @CompoundIndex(def = "{'srId':1, 'statisticsType':1, 'time':1}",
                name = "pk_spinReserveStatistics",
                unique = true),
        @CompoundIndex(def = "{'srId':1, 'statisticsType':1}",
                name = "ix_spinReserveStatistics1")
})
public class SpinReserveStatistics extends UuidDocument {

    /**
     * 即時備轉
     */
    @DBRef
    @Field("srId")
    private SpinReserveProfile spinReserveProfile;

    /**
     * 統計資料時間
     */
    private Date time;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * 統計類型
     */
    @Enumerated(EnumType.STRING)
    private StatisticsType statisticsType;

    /**
     * 場域統計資料
     */
    private List<SpinReserveStatisticsDetail> list;

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
     * 平均容量報價 //Sam 20201103
     */
    private BigDecimal avgCapacityPrice;
    /**
     * 總得標容量 //Sam 20201103
     */
    private BigDecimal totalAwardedCapacity;

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


}