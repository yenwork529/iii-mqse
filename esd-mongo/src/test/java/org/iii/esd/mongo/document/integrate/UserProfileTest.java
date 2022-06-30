package org.iii.esd.mongo.document.integrate;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import org.iii.esd.mongo.document.Hash32;

@Log4j2
public class UserProfileTest {

    private static final String TEST_EMAIL = "pds@iii.org.tw";

    @Test
    public void testGenId(){
        log.info("id: {}", Hash32.toLong(TEST_EMAIL));
    }
}
