package org.iii.esd.mongo.document.integrate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "DailyPrice")
public class DailyPrice extends BaseDocument {

    public static final int PRICE_SCALE = 2;

    private BigDecimal avgSrPrice;
    private BigDecimal avgSupPrice;
    private BigDecimal avgAfcPrice;
    private BigDecimal avgMarginalPrice;

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String buildId(Date timestamp) {
        return toLocalDateTime(timestamp).format(FORMATTER);
    }

    public static String buildId(LocalDateTime timestamp) {
        return timestamp.format(FORMATTER);
    }

    public static String buildId(LocalDate date) {
        return date.format(FORMATTER);
    }

    public DailyPrice(Date timestamp, BigDecimal avgSr, BigDecimal avgSup, BigDecimal avgAfc, BigDecimal avgMarginal) {
        super(buildId(timestamp), timestamp);

        this.avgMarginalPrice = avgMarginal;
        this.avgSrPrice = avgSr;
        this.avgSupPrice = avgSup;
        this.avgAfcPrice = avgAfc;
    }
}
