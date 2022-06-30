package org.iii.esd.afc.performance;

import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Log4j2
public class SbspmParameterInitializer {

    @Bean
    public SbspmParameter sbspmParameter(Environment env) {
        return SbspmParameter.builder()
                             .paramA(SbspmParameter.SbspmModel.builder()
                                                              .freq(getProperty(env, "afc.sbspm.A.freq", 59.50))
                                                              .normalPower(getProperty(env, "afc.sbspm.A.normalPower", 100.00))
                                                              .highPower(getProperty(env, "afc.sbspm.A.highPower", 100.00))
                                                              .lowPower(getProperty(env, "afc.sbspm.A.lowPower", 100.00))
                                                              .build())
                             .paramB(SbspmParameter.SbspmModel.builder()
                                                              .freq(getProperty(env, "afc.sbspm.B.freq", 59.75))
                                                              .normalPower(getProperty(env, "afc.sbspm.B.normalPower", 48.00))
                                                              .highPower(getProperty(env, "afc.sbspm.B.highPower", 48.00))
                                                              .lowPower(getProperty(env, "afc.sbspm.B.lowPower", 48.00))
                                                              .build())
                             .paramC(SbspmParameter.SbspmModel.builder()
                                                              .freq(getProperty(env, "afc.sbspm.C.freq", 59.98))
                                                              .normalPower(getProperty(env, "afc.sbspm.C.normalPower", 0.00))
                                                              .highPower(getProperty(env, "afc.sbspm.C.highPower", 9.00))
                                                              .lowPower(getProperty(env, "afc.sbspm.C.lowPower", -9.00))
                                                              .build())
                             .paramD(SbspmParameter.SbspmModel.builder()
                                                              .freq(getProperty(env, "afc.sbspm.D.freq", 60.02))
                                                              .normalPower(getProperty(env, "afc.sbspm.D.normalPower", 0.00))
                                                              .highPower(getProperty(env, "afc.sbspm.D.highPower", 9.00))
                                                              .lowPower(getProperty(env, "afc.sbspm.D.lowPower", -9.00))
                                                              .build())
                             .paramE(SbspmParameter.SbspmModel.builder()
                                                              .freq(getProperty(env, "afc.sbspm.E.freq", 60.25))
                                                              .normalPower(getProperty(env, "afc.sbspm.E.normalPower", -48.00))
                                                              .highPower(getProperty(env, "afc.sbspm.E.highPower", -48.00))
                                                              .lowPower(getProperty(env, "afc.sbspm.E.lowPower", -48.00))
                                                              .build())
                             .paramF(SbspmParameter.SbspmModel.builder()
                                                              .freq(getProperty(env, "afc.sbspm.F.freq", 60.50))
                                                              .normalPower(getProperty(env, "afc.sbspm.F.normalPower", -100.00))
                                                              .highPower(getProperty(env, "afc.sbspm.F.highPower", -100.00))
                                                              .lowPower(getProperty(env, "afc.sbspm.F.lowPower", -100.00))
                                                              .build())
                             .build();
    }

    private Double getProperty(Environment env, String key, Double failValue) {
        Optional<Double> oProp = Optional.ofNullable(env.getProperty(key, Double.class));

        if (!oProp.isPresent()) {
            log.error("can't get property {}", key);
        }

        return oProp.orElse(failValue);
    }
}
