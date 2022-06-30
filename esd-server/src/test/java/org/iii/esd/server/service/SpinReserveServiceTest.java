package org.iii.esd.server.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.service.SiloCompanyProfileService;
import org.iii.esd.mongo.service.SpinReserveService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        SpinReserveService.class,
        SiloCompanyProfileService.class})
@EnableAutoConfiguration
@Log4j2
public class SpinReserveServiceTest extends AbstractServiceTest {

    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private SiloCompanyProfileService siloCompanyProfileService;

    @Test
    public void testLoadContext() {
        assertThat(spinReserveService).isNotNull();
        assertThat(siloCompanyProfileService).isNotNull();
    }

    @Test
    public void testGetCompanyFromSpinReserveProfile() {

    }
}
