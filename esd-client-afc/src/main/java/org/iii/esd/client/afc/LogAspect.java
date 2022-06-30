package org.iii.esd.client.afc;

import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@EnableAspectJAutoProxy
@Component
public class LogAspect {
	
	/* 
	 * execution(access_specifier package_name class_name method_name(argument_list))
	 * within(cc.openhome.model.AccountDAO) 相當於 execution(* cc.openhome.model.AccountDAO.*(..))
	 * within(cc.openhome.model.*) 相當於 execution(* cc.openhome.model.*.*(..))
	 * within(cc.openhome.model..*) 相當於 execution(* cc.openhome.model.service..*.*(..))
	 */
    @Around("@annotation(org.iii.esd.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
        final Object proceed = joinPoint.proceed();
        log.debug("{} executed in {} ms", joinPoint.getStaticPart(), stopwatch.getTime());
        return proceed;
    }

}