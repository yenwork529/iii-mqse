package org.iii.esd.server.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthenticationUtil {
    private AuthenticationUtil(){}

    public static String getUserEmailFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
