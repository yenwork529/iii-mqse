package org.iii.esd.client.controllers.rest;

import java.nio.file.AccessDeniedException;

import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.exception.Error;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Log4j2
public class ErrorController {
	
    @ExceptionHandler({
    	BindException.class,
    	JsonProcessingException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class	
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse exception400(final NestedRuntimeException ex) {
    	return new ErrorResponse(Error.badRequest, ex.getRootCause().getMessage());
    }
	
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse exception403() {
		return new ErrorResponse(Error.forbidden);
    }    
    
	@ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound() {
		return new ErrorResponse(Error.notFound);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse methodNotAllowed(final HttpRequestMethodNotSupportedException ex) {
    	return new ErrorResponse(Error.methodNotAllowed, ex.getMethod());
    }	
    
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse internalServerError(final Throwable throwable) {
    	log.error(throwable);
        String errorMessage = (throwable != null && throwable.getMessage()!=null ? throwable.getMessage() : "Unknown error");
    	return new ErrorResponse(Error.internalServerError, errorMessage);
    }    

}