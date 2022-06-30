package org.iii.esd.server.controller;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Log4j2
public class SiloSiloUserControllerTest {

    @Test
    public void testPasswordEncoder() {
        String password = "agretechco123!";
        String encoded = (new BCryptPasswordEncoder()).encode(password);
        log.info("password {} encoded {}", password, encoded);
    }
}
