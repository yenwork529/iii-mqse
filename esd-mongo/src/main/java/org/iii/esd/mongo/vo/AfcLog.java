package org.iii.esd.mongo.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfcLog {

    private Long afcId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    private String voltageA;

    private String voltageB;

    private String voltageC;

    private String frequency;

    private String essPower;

    private String essPowerRatio;

    private String activePower;

    private String kvar;

    private String powerFactor;

    private String sbspm;

    private String spm;

    private String soc;

    @Override
    public String toString() {
        return "afcId=" + getAfcId() + ", timestamp=" + getTimestamp() + ", frequency=" + getFrequency() + ", essPower="
                + getEssPower() + ", essPowerRatio=" + getEssPowerRatio() + ", sbspm=" + getSbspm() + ", spm="
                + getSpm();
    }
}
