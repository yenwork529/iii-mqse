package org.iii.esd.mongo.document.integrate;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import org.iii.esd.mongo.document.Hash32;

@Log4j2
public class Hash32Test {

    @Test
    public void testHash32(){
        String testString = "res4@iii.org.tw";
        log.info("hash32: {}", Hash32.toLong(testString));
    }
}
