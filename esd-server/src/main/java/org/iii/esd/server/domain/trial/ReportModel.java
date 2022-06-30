package org.iii.esd.server.domain.trial;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.utils.TypedPair;

import static org.iii.esd.utils.TypedPair.cons;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ReportModel {

    private String qseCode;
    private String txgCode;
    private String resCode;
    private String serviceType;
    private String timestamp;
    private String frequency;
    private String voltageA;
    private String voltageB;
    private String voltageC;
    private String currentA;
    private String currentB;
    private String currentC;
    private String activePower;
    private String genEnergy;
    private String drEnergy;
    private String kvar;
    private String powerFactor;
    private String soc;

    // HEADER:
    // QSE_ID,GROUP_ID,RESOURCE_ID,SERVICE_ITEM,DATA_TIMESTAMP,HZ,V_A,V_B,V_C,A_A,A_B,A_C,TOT_W,SUPWH,DMDWH,TOT_VAR,TOT_PF,SOC
    public static final List<TypedPair<String>> HEADER_NAME_MAPPING =
            ImmutableList.<TypedPair<String>>builder()
                    .add(cons("QSE_ID", "qseCode"))
                    .add(cons("GROUP_ID", "txgCode"))
                    .add(cons("RESOURCE_ID", "resCode"))
                    .add(cons("SERVICE_ITEM", "serviceType"))
                    .add(cons("DATA_TIMESTAMP", "timestamp"))
                    .add(cons("HZ", "frequency"))
                    .add(cons("V_A", "voltageA"))
                    .add(cons("V_B", "voltageB"))
                    .add(cons("V_C", "voltageC"))
                    .add(cons("A_A", "currentA"))
                    .add(cons("A_B", "currentB"))
                    .add(cons("A_C", "currentC"))
                    .add(cons("TOT_W", "activePower"))
                    .add(cons("SUPWH", "genEnergy"))
                    .add(cons("DMDWH", "drEnergy"))
                    .add(cons("TOT_VAR", "kvar"))
                    .add(cons("TOT_PF", "powerFactor"))
                    .add(cons("SOC", "soc"))
                    .build();
}
