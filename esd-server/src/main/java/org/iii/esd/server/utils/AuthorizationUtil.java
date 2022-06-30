package org.iii.esd.server.utils;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;

public final class AuthorizationUtil {
    private AuthorizationUtil() {}

    private static String toString(long roleId) {
        return Long.valueOf(roleId).toString();
    }

    public static boolean isSysLevel(long roleId) {
        return ROLE_SYSADMIN.equals(toString(roleId));
    }

    public static boolean isQseLevel(long roleId) {
        return ROLE_QSEADMIN.equals(toString(roleId))
                || ROLE_QSEUSER.equals(toString(roleId));
    }

    public static boolean isTxgLevel(long roleId) {
        return ROLE_SIADMIN.equals(toString(roleId))
                || ROLE_SIUSER.equals(toString(roleId));
    }

    public static boolean isResLevel(long roleId) {
        return ROLE_FIELDADMIN.equals(toString(roleId))
                || ROLE_FIELDUSER.equals(toString(roleId));
    }

}
