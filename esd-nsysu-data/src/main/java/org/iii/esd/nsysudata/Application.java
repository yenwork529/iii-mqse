package org.iii.esd.nsysudata;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.iii.esd.nsysudata.service.MdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.extern.log4j.Log4j2;

@ComponentScan(basePackages = { "org.iii.esd" })
@SpringBootApplication
@ServletComponentScan
@Log4j2
public class Application implements ServletContextInitializer {
	
	@Autowired
	private MdService mdService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
    	Map<String, String> map= mdService.getDeviceId();
    	for (String key: map.keySet()) {
    		servletContext.setInitParameter(key, map.get(key).toString());
    		log.debug(key+ " " + map.get(key));
		}
    }

	@Bean
	public WebServerFactoryCustomizer<UndertowServletWebServerFactory> customizer() {
	    return new WebServerFactoryCustomizer<UndertowServletWebServerFactory>() {
			@Override
			public void customize(UndertowServletWebServerFactory factory) {
				factory.addDeploymentInfoCustomizers(deploymentInfo -> {
					WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
					webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(false, 1024));
					deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
				});
			}
	    };
	}	

}