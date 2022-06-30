package org.iii.esd.jwt.endpoint;

import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.exception.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.log4j.Log4j2;

@ControllerAdvice
@Log4j2
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<?> customHandle() {
		log.error("request unauthorized");
    	return ResponseEntity.ok(new ErrorResponse(Error.unauthorized));
    }
    
}
