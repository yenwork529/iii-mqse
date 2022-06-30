package org.iii.esd.jwt.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.exception.Error;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Log4j2
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

//    @Autowired
//    private TokenProvider tokenProvider;
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
//		Payload payload = tokenProvider.getPayload(request);
//		log.info(((CsrfToken) request.getAttribute(CsrfToken.class.getName())).getHeaderName() );
		log.warn(request.getRequestURI());
		response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().print(new ObjectMapper().writeValueAsString(new ErrorResponse(Error.forbidden)));
	}

}