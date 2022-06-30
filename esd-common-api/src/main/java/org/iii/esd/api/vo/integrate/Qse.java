package org.iii.esd.api.vo.integrate;

import java.util.Date;
import javax.validation.constraints.Positive;

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
@ToString
@Builder
public class Qse {
    @Positive(message = "id is Invalid")
    private Long id;

    private String qseId;

    private String companyId;

    private String name;

    private Integer qseCode;

    private String dnpUrl;

    private String vpnLanIp;

    private String vpnWanIp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTimestamp;
}
