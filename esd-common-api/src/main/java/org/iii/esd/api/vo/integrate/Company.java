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
public class Company {
    @Positive(message = "id is Invalid")
    private Long id;

    private String companyId;

    private String name;

    private String fullName;

    private String address;

    private String phone;

    private String contractPerson;


    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTimestamp;
}
