package org.iii.esd.client;

import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.ComponentScan;

import org.iii.esd.enums.Listener;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.thirdparty.socket.ReceiveServer;

@ComponentScan(basePackages = {"org.iii.esd"})
@SpringBootApplication
@ServletComponentScan
@Log4j2
public class Application implements ServletContextInitializer {

    @Autowired
    private FieldProfileService fieldProfileService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        Integer receive = Listener.THINCLIENT.getPort();
        try {
            for (String p : args) {
                receive = Listener.valueOf(p).getPort();
                break;
            }
            log.info(receive);
            new ReceiveServer(receive).start();
        } catch (IllegalArgumentException e) {
            log.error("error args:{}", args[0]);
        }
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        String fieldIds = fieldProfileService.findEnableFieldProfile()
                                             .stream()
                                             .map(f -> f.getId().toString())
                                             .collect(Collectors.joining(","));

        log.info("init field ids {}", fieldIds);

        servletContext.setInitParameter("fieldIds", fieldIds);
    }
}