package org.iii.esd.api.vo.integrate;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Res {
    @Positive(message = "id is Invalid")
    private Long id;

    private String resId;

    private String companyId;

    private String txgId;

    private Integer resCode;

    private Integer resType;

    private String name;

    private String tcUrl;

    /**
     * ThinClient資料最後上傳時間
     */
    private Date tcLastUploadTime;

    /**
     * ThinClient 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus tcEnable;

    /**
     * 設備連線狀態
     */
    @Enumerated(EnumType.STRING)
    private ConnectionStatus devStatus;

    /**
     * 場域誤差因子
     */
    private BigDecimal accFactor;

    private BigDecimal registerCapacity;

    private Date createTimestamp;

    private Date updateTimestamp;
}
