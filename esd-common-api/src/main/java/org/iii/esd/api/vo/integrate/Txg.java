package org.iii.esd.api.vo.integrate;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.enums.EnableStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Txg {
    @Positive(message = "id is Invalid")
    private Long id;

    private String txgId;

    private String companyId;

    private String qseId;

    private Integer txgCode;

    private Integer serviceType;

    private String name;

    private Date tcLastUploadTime;

    /**
     * 競標契約容量( kW)
     */
    private BigDecimal registerCapacity;

    /**
     * line 憑證
     */
    private String lineToken;

    /**
     * 效能價格($/MW)
     */
    private BigDecimal efficiencyPrice;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTimestamp;

    private EnableStatus enableStatus;
}
