package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class SpinReserveBidData {

    public static final String TYPE_DISPATCH = "DISPATCH";
    public static final String TYPE_BID = "BID";
    public static final String TYPE_ABANDON = "ABANDON";

    private String type;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date begin;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date end;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal capacity;
}
