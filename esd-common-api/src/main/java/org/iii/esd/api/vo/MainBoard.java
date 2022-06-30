package org.iii.esd.api.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.utils.Range;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MainBoard {

    /**
     * 組織資訊 (含交易群組與資源)
     */
    private OrgInfo org;

    /**
     * 狀態
     */
    private State state;

    /**
     * 目前指標
     */
    private CurrentIndices current;

    /**
     * 今日指標
     */
    private TodayIndices today;

    /**
     * 服務狀態
     */
    public enum State {
        /**
         * 待命中
         */
        STAND_BY,

        /**
         * 調度中
         */
        DISPATCH,

        /**
         * 中止待命
         */
        ABANDON,

        /**
         * 未得標
         */
        NOT_AWARDED,

        /**
         * 未投標
         */
        NOT_BIDDING,

        /**
         * 待命中(準備量不足)
         */
        NOT_ENOUGH,
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class OrgInfo {
        /**
         * 交易群組 ID
         */
        private String txgId;

        /**
         * 交易群組名稱
         */
        private String txgName;

        /**
         * 註冊容量
         */
        private BigDecimal registryCapacity;

        /**
         * 服務類型
         */
        private Integer serviceType;

        /**
         * 資源類型
         */
        private Integer resourceType;

        /**
         * 資源數量
         */
        private Integer resourceCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class TodayIndices {

        public static final TodayIndices EMPTY = TodayIndices.builder()
                                                             .awardedHours(0)
                                                             .build();

        /**
         * 得標總時數
         */
        private int awardedHours;

        /**
         * 得標區間
         */
        private Range<String> awardedPeriod;

        /**
         * 得標容量範圍
         */
        private Range<String> awardedRange;

        /**
         * 容量費範圍
         */
        private Range<String> capacityPriceRange;

        /**
         * 電能費範圍
         */
        private Range<String> energyPriceRange;

        /**
         * 中止待命區間
         */
        private Range<String> abandonPeriod;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class CurrentIndices {
        public static final CurrentIndices EMPTY = new CurrentIndices();

        /**
         * 得標量
         */
        private String awardedCapacity;

        /**
         * 調度量
         */
        private String clippedCapacity;

        /**
         * 調度時間
         */
        private String noticeTime;

        /**
         * 降載區間
         */
        private Range<String> clippedPeriod;
    }
}
