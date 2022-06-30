package org.iii.esd.jwt.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.iii.esd.jwt.security.TokenProvider;
import org.iii.esd.jwt.security.UserinfoDetails;
import org.iii.esd.jwt.security.UserinfoDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AuthenticationFilter extends OncePerRequestFilter {
	
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserinfoDetailsService userinfoDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
        try {
            String token = tokenProvider.getTokenFromRequest(request);
        	log.debug(token);
            if(StringUtils.hasText(token)) {
            	log.debug(request.getRemoteHost() + " " + request.getRequestURI());
            	if(request.getRequestURI().indexOf("/thinclient/") == 0) {
            		log.debug(request.getRemoteHost() + " " + request.getRequestURI());
            		//log.info(request.getReader().lines().collect(Collectors.toList()));
            		//EncryptUtils.genThinClientToken(fieldId, request.getRemoteHost());
            	}else if(tokenProvider.validateToken(token)) {
                    String email = tokenProvider.getEmailFromToken(token);
                    UserinfoDetails userinfoDetails = userinfoDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = 
                    		new UsernamePasswordAuthenticationToken(userinfoDetails, null, userinfoDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication); 
                    //Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();            		
            	}else {
            		log.info(request.getRemoteHost() + " " + request.getRequestURI());
            	}
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex.getMessage());
        }
        filterChain.doFilter(request, response);
	}

}