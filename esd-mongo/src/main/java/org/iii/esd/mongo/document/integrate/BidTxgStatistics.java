package org.iii.esd.mongo.document.integrate;

import lombok.*;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.UuidDocument;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "BidTxgStatistics")
public class BidTxgStatistics extends UuidDocument {

    private String txgId;
    private long timeticks;
    private Date timestamp;

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

    // TODO:建立日績效和月績效會重疊id
    public BidTxgStatistics(String txgId, Date dt, StatisticsType type) {
        SimpleDateFormat st = new SimpleDateFormat("MM");
        String monthString = st.format(dt.getTime());
        int month = Integer.parseInt(monthString);

        this.txgId = txgId;
        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        if (type == StatisticsType.day) {
            super.id = txgId + String.format("_%d", hour);
        } else {
            super.id = txgId + String.format("_%d%d", hour, month);
        }
    }

    @Transient
    @BsonIgnore
    private List<BidResStatistics> list;

    public static BidTxgStatistics fromBid(BidTxgBid bidTxgBid) {
        List<BidResStatistics> list = bidTxgBid.getList()
                .stream()
                .map(BidResStatistics::fromBid)
                .collect(Collectors.toList());

        return BidTxgStatistics.builder()
                .list(list)
                .txgId(bidTxgBid.getTxgId())
                .timestamp(bidTxgBid.getTimestamp())
                .timeticks(bidTxgBid.getTimeticks())
                .capacityRevenue(bidTxgBid.getCapacityRevenue())
                .efficacyRevenue(bidTxgBid.getEfficacyRevenue())
                .energyRevenue(bidTxgBid.getEnergyRevenue())
                .build();
    }

    private Integer Add(Integer a, Integer b) {
        if (b == null) {
            return a;
        }
        if (a == null) {
            return 0;
        }
        return a + b;

    }

    private BigDecimal Add(BigDecimal a, BigDecimal b) {
        if (b == null) {
            return a;
        }
        if (a == null) {
            return BigDecimal.ZERO;
        }
        return a.add(b);
    }

    public BidTxgStatistics add(BidTxgStatistics bi) {
        noticeCount = Add(noticeCount, bi.getNoticeCount());
        awardedCount = Add(awardedCount, bi.getAwardedCount());
        avgRevenueFactor = Add(avgRevenueFactor, bi.getAvgRevenueFactor());
        avgCapacityPrice = Add(avgCapacityPrice, bi.getAvgCapacityPrice());
        totalAwardedCapacity = Add(totalAwardedCapacity, bi.getTotalAwardedCapacity());
        capacityRevenue = Add(capacityRevenue, bi.getCapacityRevenue());
        efficacyRevenue = Add(efficacyRevenue, bi.getEfficacyRevenue());
        energyRevenue = Add(energyRevenue, bi.getEnergyRevenue());
        return this;
    }

    public boolean isZero(BigDecimal b){
        if(b == null){
            return true;
        }
        if(b.intValue() == 0){
            return true;
        }
        return false;
    }

    public boolean isZeroAvgRevenueFactor(){
        return isZero(avgRevenueFactor);
    }

    public boolean isZeroAvgCapacityPrice(){
        return isZero(avgCapacityPrice);
    }

    public BidTxgStatistics divideAvgRevenueFactor(int cnt){
        if(cnt != 0){
            avgRevenueFactor = avgRevenueFactor.divide(BigDecimal.valueOf((long)cnt), 2, RoundingMode.HALF_UP);
        }
        return this;
    }

    public BidTxgStatistics divideAvgCapacityPrice(int cnt){
        if(cnt != 0){
            avgCapacityPrice = avgCapacityPrice.divide(BigDecimal.valueOf((long)cnt), 2, RoundingMode.HALF_UP);
        }
        return this;
    }
}
