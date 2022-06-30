package org.iii.esd.jwt.security;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import org.iii.esd.exception.Error;
import org.iii.esd.mongo.service.integrate.UserService;

@Component
@Log4j2
public class LoginAuthenticationProvider extends DaoAuthenticationProvider {

    public static final int MAX_RETRY = 5;
    public static final int BAN_TIME = 60;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("userinfoDetailsService")
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();

        try {
            setPasswordEncoder(SecurityConfig.passwordEncoder());

            Authentication auth = super.authenticate(authentication);

            // if reach here, means login success, else exception will be thrown
            // 登入失敗5次會封鎖60分鐘
            if (userService.checkPasswordRetryAttempts(email, MAX_RETRY, BAN_TIME)) {
                throw new LockedException(Error.accountlocked.getMsg());
            }

            // reset the user attempts
            userService.resetPasswordRetry(email);

            return auth;
        } catch (BadCredentialsException e) {
            // invalid login, update user attempts
            userService.updatePasswordRetryAttempts(email);
            log.error(ExceptionUtils.getStackTrace(e));
            throw e;
        } catch (Exception e) {
            // this account is locked
            log.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

}