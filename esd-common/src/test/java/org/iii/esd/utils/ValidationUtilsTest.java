package org.iii.esd.utils;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.iii.esd.utils.ValidationUtils.isPasswordValid;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class ValidationUtilsTest {

    @Test
    void testIsPasswordValid() {

        String pw1 = "password!";
        String pw2 = "password1";
        String pw3 = "pw3!";
        String pw4 = "password1!";
        String pw5 = "!@#$%^&*1a";

        log.info(isPasswordValid(pw1));
        assertFalse(isPasswordValid(pw1));
        assertFalse(isPasswordValid(pw2));
        assertFalse(isPasswordValid(pw3));
        assertTrue(isPasswordValid(pw4));
        assertTrue(isPasswordValid(pw5));

    }

}
