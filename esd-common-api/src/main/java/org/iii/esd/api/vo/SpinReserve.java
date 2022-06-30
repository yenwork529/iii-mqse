package org.iii.esd.api.vo;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

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
public class SpinReserve {

    @Positive(message = "id is Invalid")
    private Long id;

    private String name;

    private Integer code;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTimestamp;

    @Positive(message = "companyId is Invalid")
    private Long companyId;

    private String companyName;

    private String dnpURL;

    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;

    private Integer bidContractCapacity;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date noticeTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date startTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date endTimestamp;

    private Integer clipKW;

    public SpinReserve(SpinReserveProfile spinReserveProfile) {
        this.id = spinReserveProfile.getId();
        this.name = spinReserveProfile.getName();
        this.updateTimestamp = spinReserveProfile.getUpdateTime();
        this.createTimestamp = spinReserveProfile.getCreateTime();
        SiloCompanyProfile siloCompanyProfile = spinReserveProfile.getSiloCompanyProfile();
        if (siloCompanyProfile != null) {
            this.companyId = siloCompanyProfile.getId();
            this.companyName = siloCompanyProfile.getName();
        }
        this.dnpURL = spinReserveProfile.getDnpURL();
        this.enableStatus = spinReserveProfile.getEnableStatus();
        this.bidContractCapacity = spinReserveProfile.getBidContractCapacity();
        this.noticeTimestamp = spinReserveProfile.getNoticeTime();
        this.startTimestamp = spinReserveProfile.getStartTime();
        this.endTimestamp = spinReserveProfile.getEndTime();
        this.clipKW = spinReserveProfile.getClipKW();
    }

}