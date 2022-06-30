package org.iii.esd.api.vo.integrate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.utils.DatetimeUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class DRegData {

    public static final BigDecimal DEFAULT_BASE_FREQ = BigDecimal.valueOf(60.00);

    public static final int VOLTAGE_PU_SCALE = 3;

    public static synchronized DRegData.DRegDataBuilder builderOfTimeticks(long timeticks) {
        return builder().timeticks(timeticks)
                        // .timestamp(new Date(timeticks))
                        .timeTxt(getTimeTxtFromTimeticks(timeticks));
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static String getTimeTxtFromTimeticks(long timeticks) {
        LocalDateTime dt = DatetimeUtils.toLocalDateTime(new Date(timeticks));
        return dt.format(FORMATTER);
        // return FORMATTER.format(new Date(timeticks));
    }

    public DRegData(long timeticks) {
        this.timeticks = timeticks;
        // this.timestamp = new Date(timeticks);
        // LocalDateTime dt = DatetimeUtils.toLocalDateTime(this.timestamp);
        LocalDateTime dt = DatetimeUtils.toLocalDateTime(new Date(timeticks));
        this.timeTxt = dt.format(FORMATTER);
    }

    private long timeticks;
    // private Date timestamp;
    private String timeTxt;

    // Main Monitor Fields
    private BigDecimal voltageA;
    private BigDecimal voltageB;
    private BigDecimal voltageC;
    private BigDecimal currentA;
    private BigDecimal currentB;
    private BigDecimal currentC;
    private BigDecimal frequency;
    private BigDecimal powerFactor;
    private BigDecimal kW;
    private BigDecimal kVar;
    private BigDecimal energyIMP;
    private BigDecimal energyEXP;
    private BigDecimal soc;
    private BigDecimal status;
    private BigDecimal sbspm;
    private BigDecimal baseFreq;

    // BidInfo
    private BigDecimal awardedCapacity;
    private BigDecimal abandon;
    private BigDecimal targetFreq;

    // ResDetail
    private List<ResDetail> res;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class ResDetail {
        private String resId;
        private BigDecimal kw;
    }
}
