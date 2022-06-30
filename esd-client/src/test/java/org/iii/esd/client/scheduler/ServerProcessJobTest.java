package org.iii.esd.client.scheduler;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.client.service.AbstractServiceTest;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

@EnableAutoConfiguration
@ContextConfiguration(
        classes = {
                ServerProcessJob.class,
                FieldProfileService.class,
        }
)
@Log4j2
public class ServerProcessJobTest extends AbstractServiceTest {

    @Autowired
    private ServerProcessJob serverProcessJob;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Value("#{'${fieldIds}'.split(',')}")
    private Set<Long> fieldIds;

    @Value("${delay}")
    private int delay;

    @Test
    public void testGetFields() {
        List<FieldProfile> list = init();
        for (FieldProfile profile : list) {
            log.info(profile.toString());
        }
    }

    private List<FieldProfile> init() {
        log.debug(fieldIds);
        fieldIds.remove(null);
        return fieldProfileService.find(fieldIds);
    }

    @Test
    public void testRun() throws Exception {
        List<FieldProfile> list = init();

        for (FieldProfile fieldProfile : list) {
            Long fieldId = fieldProfile.getId();
            log.debug("ServerProcessJob fieldId:{} starting. ", fieldId);
            if(!EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
                serverProcessJob.run(fieldProfile, delay);
            }else {
                log.warn("Field:{}({}) is not enable.", fieldProfile.getName(), fieldProfile.getId());
            }
            log.debug("ServerProcessJob fieldId:{} finished. ", fieldId);
        }
    }
}
