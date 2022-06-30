package org.iii.esd.nsysudata;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
public class LogAspect {
	
	private Long milliseconds;

	//@Pointcut("within(org.iii.esd.nsysudata.scheduler..*)")
	@Pointcut("execution(* org.iii.esd.nsysudata.scheduler..*(..))")
	public void pointcut() {

	}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
    	milliseconds = new Date().getTime();
    }

    @After("pointcut()")
    public void after(JoinPoint joinPoint) {
    	log.info("total {} milliseconds", new Date().getTime()-milliseconds);
    }		
	
}