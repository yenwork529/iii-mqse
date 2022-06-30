package org.iii.esd.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class DataConfigurationTest {

    @Autowired
    private DataConfiguration dataConfig;

    @Test
    public void testLoadConfig(){
        log.info("load data {}", dataConfig);
    }
}
