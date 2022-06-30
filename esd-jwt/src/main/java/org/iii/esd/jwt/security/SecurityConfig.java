package org.iii.esd.jwt.security;

import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import org.iii.esd.api.RestConstants;
import org.iii.esd.jwt.filter.AuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@Log4j2
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoginAuthenticationProvider loginAuthenticationProvider;
    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private JwtAccessDeniedHandler accessDeniedHandler;
    @Autowired
    private UserinfoDetailsService userinfoDetailsService;
    @Autowired
    private AuthenticationFilter authenticationFilter;
    @Value("${permit}")
    private String[] permit;

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("WebSecurityConfiguration.configure AuthenticationManagerBuilder={}", auth);
        auth.userDetailsService(userinfoDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests().antMatchers(permit).permitAll()
            .anyRequest().authenticated().and()
            .authenticationProvider(loginAuthenticationProvider).cors().and()
            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .requireCsrfProtectionMatcher(request ->
                    Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                          .contains(HttpMethod.valueOf(request.getMethod()))
                            && request.getRequestURI().startsWith(RestConstants.FUNCTION_ESD + "/123")).and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler).and()
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}