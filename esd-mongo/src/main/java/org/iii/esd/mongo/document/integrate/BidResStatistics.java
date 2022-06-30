package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import org.iii.esd.enums.StatisticsType;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.mongo.document.UuidDocument;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "BidResStatistics")

public class BidResStatistics extends UuidDocument {
    private String resId;
    private Date timestamp;
    private Long timeticks;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * 資料建立時間
     */
    @JsonIgnore
    private Date createTime;

    /**
     * 統計類型
     */
    @Enumerated(EnumType.STRING)
    private StatisticsType statisticsType;

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
    private Integer noticeCount;

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
     * 效能費
     */
    private BigDecimal efficacyRevenue;

    /**
     * 新版電能費
     */
    private BigDecimal energyRevenue;

    /**
     * dReg 執行率
     */
    private BigDecimal sbspm;

    /**
     * 服務指標
     */
    private BigDecimal servingIndex;

    public BidResStatistics(String resId, Date dt, StatisticsType type) {
        SimpleDateFormat st = new SimpleDateFormat("MM");
        String monthString = st.format(dt.getTime());
        int month = Integer.parseInt(monthString);

        this.resId = resId;
        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        if (type == StatisticsType.day) {
            super.id = resId + String.format("_%d", hour);
        } else {
            super.id = resId + String.format("_%d%d", hour, month);
        }
    }

    public static BidResStatistics fromBid(BidResBid bidResBid) {
        return BidResStatistics.builder()
                .resId(bidResBid.getResId())
                .timestamp(bidResBid.getTimestamp())
                .timeticks(bidResBid.getTimeticks())
                .capacityRevenue(bidResBid.getCapacityRevenue())
                .efficacyRevenue(bidResBid.getEfficacyRevenue())
                .energyRevenue(bidResBid.getEnergyRevenue())
                .build();
    }
}
