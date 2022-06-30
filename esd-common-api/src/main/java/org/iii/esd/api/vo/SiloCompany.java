package org.iii.esd.api.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.mongo.document.SiloCompanyProfile;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SiloCompany {

    private Long id;

    private String name;

    private Integer qseCode;

    private Integer tgCode;

    private Integer serviceType;

    private String dnpURL;

    private String callbackURL;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTime;

    public SiloCompany(SiloCompanyProfile siloCompanyProfile) {
        this.id = siloCompanyProfile.getId();
        this.name = siloCompanyProfile.getName();
        this.qseCode = siloCompanyProfile.getQseCode();
        this.tgCode = siloCompanyProfile.getTgCode();
        this.serviceType = siloCompanyProfile.getServiceType();
        this.dnpURL = siloCompanyProfile.getDnpURL();
        this.callbackURL = siloCompanyProfile.getCallbackURL();
        this.updateTime = siloCompanyProfile.getUpdateTime();
        this.createTime = siloCompanyProfile.getCreateTime();
    }

}