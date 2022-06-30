package org.iii.esd.caculate;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.iii.esd.afc.def.FPMappingEnum;
import org.iii.esd.afc.def.RangeEnum;
import org.iii.esd.afc.def.ZoneEnum;

import static org.junit.jupiter.api.Assertions.fail;

@EnableAutoConfiguration
@Log4j2
public class AfcEnumTest {

    @Test
    public void testZoneEnum() {
        ZoneEnum fullBandOutputZone = ZoneEnum.of(59.50);
        if (fullBandOutputZone == null || ZoneEnum.FULL_BAND_OUTPUT != fullBandOutputZone) {
            log.error(fullBandOutputZone);
            fail();
        }
        log.info(fullBandOutputZone);
    }

    @Test
    public void testRangeEnum() {
        RangeEnum range = RangeEnum.of(8.0);
        log.info(range);
    }

    @Test
    @Deprecated
    public void testFPMappingEnum() {
        FPMappingEnum mapping = FPMappingEnum.of(59.60, 80.0);
        if (mapping == FPMappingEnum.UNDEFINED_MAPPING || mapping == null) {
            fail();
        }
        log.info(mapping);
    }
}
