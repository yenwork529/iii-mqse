package org.iii.esd.utils;

import org.springframework.core.env.Environment;

public class SystemsUtils {

    public static String getActive(Environment environment) {
        if (environment != null) {
            String[] activeProfiles = environment.getActiveProfiles();
            return activeProfiles.length > 0 ? activeProfiles[0] : "";
        } else {
            return "";
        }
    }

}
