package org.iii.esd.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
@Aspect
@Component
public class ApiLogAspect {

    private long time;

    @Pointcut("within(org.iii.esd.server.controllers..*)")
    public void apiLog() {

    }

    @Before("apiLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        time = System.currentTimeMillis();
        dumpRequest(joinPoint);
    }

    // @AfterReturning(returning = "responseObject",
    //         pointcut = "apiLog()")
    // public void dumpResponse(Object responseObject) throws Throwable {
    //     log.debug("==========API RESPONSE==========");
    //     log.debug("Time Spent: {} sec.", (System.currentTimeMillis() - time) / 1000d);
    //     log.info(new Gson().toJson(responseObject));
    //     log.debug("==========API RESPONSE==========");
    // }

    private void dumpRequest(JoinPoint joinPoint) throws IOException {
        // ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // HttpServletRequest request = attributes.getRequest();
        // String httpMethod = request.getMethod();
        // log.debug("==========API RESQUEST==========");
        // log.debug("Request : {} {}", httpMethod, request.getRequestURL().toString());
        // log.debug("RemoteHost : {}", request.getRemoteHost());
        // log.debug("RemoteAddr : {}", request.getRemoteAddr());

        // Collections.list(request.getHeaderNames()).forEach(headerName -> {
        //     log.debug("Header : {{}}={{}}", headerName, request.getHeader(headerName));
        // });

        // Collections.list(request.getParameterNames()).forEach(parameterName -> {
        //     log.debug("Parameter : {{}}={{}}", parameterName, request.getParameter(parameterName));
        // });

        // if (!HttpMethod.GET.name().equals(httpMethod)) {
        //     Object[] argArray = joinPoint.getArgs();
        //     if (argArray != null && argArray.length > 0) {
        //         Arrays.asList(joinPoint.getArgs()).forEach(requestBody -> {
        //             log.debug("RequestBody : " + requestBody.toString());
        //         });
        //     }
        // }
        // log.debug("==========API RESQUEST==========");
    }

}
