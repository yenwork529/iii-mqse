package org.iii.esd.initializer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.iii.esd.config.Constants.PROFILE_SERVER;

@SpringBootTest
@Slf4j
public class InitializerTest {

    @Autowired
    private Initializer initializer;

    @Value("${spring.profiles.active}")
    private String env;

    @Test
    public void testInitServer() {
        initializer.initData(PROFILE_SERVER);
    }
}
