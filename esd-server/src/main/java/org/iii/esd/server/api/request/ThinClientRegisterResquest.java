package org.iii.esd.server.api.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThinClientRegisterResquest {

    @NotBlank(message = "ThinClient IP Address is Required")
    @Pattern(regexp = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$",
            message = "IP is Invalid")
    private String tcip;

    @NotNull(message = "FieldId is Required")
    @Min(value = 1)
    private Long fieldId;

}
