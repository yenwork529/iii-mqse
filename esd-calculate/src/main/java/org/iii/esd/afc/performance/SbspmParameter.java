package org.iii.esd.afc.performance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SbspmParameter {

    private SbspmModel paramA;
    private SbspmModel paramB;
    private SbspmModel paramC;
    private SbspmModel paramD;
    private SbspmModel paramE;
    private SbspmModel paramF;

    @Setter
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SbspmModel {
        private Double freq;
        private Double normalPower;
        private Double highPower;
        private Double lowPower;
    }
}
