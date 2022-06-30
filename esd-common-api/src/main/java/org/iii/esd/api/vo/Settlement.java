package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Settlement {
    /**
     * 交易日期/小時(per hour)
     */
    private Date dateTime;

    /**
     * 調頻備轉結清價格
     */
    private BigDecimal dreg;

    /**
     * 即時備轉結清價格
     */
    private BigDecimal sr;

    /**
     * 補充備轉結清價格
     */
    private BigDecimal sup;

    /**
     * 電能邊際價格
     */
    private BigDecimal marginal;
}
