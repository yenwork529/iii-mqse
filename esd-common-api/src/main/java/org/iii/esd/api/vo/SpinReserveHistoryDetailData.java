package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class SpinReserveHistoryDetailData {

    private List<String> names;
    private List<SpinReserveHistoryFieldDetail> values;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SpinReserveHistoryFieldDetail {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        private Date time;
        private List<BigDecimal> activePower;
    }
}
