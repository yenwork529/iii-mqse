package org.iii.esd.api.vo.integrate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EnergyPrice {
    private String localDate;
    private String localTime;
    private String dregPrice;
    private String srPrice;
    private String supPrice;
    private String marginalPrice;

    private LocalDateTime localDateTime;
    private BigDecimal dreg;
    private BigDecimal sr;
    private BigDecimal sup;
    private BigDecimal marginal;

    public EnergyPrice(
            String localDate,
            String localTime,
            String dregPrice,
            String srPrice,
            String supPrice,
            String marginalPrice) {
        this.localDate = localDate;
        this.localTime = localTime;
        this.dregPrice = dregPrice;
        this.srPrice = srPrice;
        this.supPrice = supPrice;
        this.marginalPrice = marginalPrice;

        trans();
    }

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-d");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void trans() {
        LocalDate ld = LocalDate.parse(localDate, DATE_FORMATTER);
        LocalTime lt = LocalTime.parse(localTime, TIME_FORMATTER);
        localDateTime = ld.atTime(lt);

        dreg = BigDecimal.valueOf(Double.parseDouble(dregPrice));
        sr = BigDecimal.valueOf(Double.parseDouble(srPrice));
        sup = BigDecimal.valueOf(Double.parseDouble(supPrice));
        marginal = BigDecimal.valueOf(Double.parseDouble(marginalPrice));
    }
}
