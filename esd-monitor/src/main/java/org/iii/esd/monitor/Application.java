package org.iii.esd.monitor;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.extern.log4j.Log4j2;
import org.iii.esd.enums.Listener;
import org.iii.esd.thirdparty.socket.ReceiveServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = {"org.iii.esd"})
@SpringBootApplication
@ServletComponentScan
@Configuration
@Log4j2
public class Application {

//	@Autowired
//	Environment env;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        if (args.length > 0) {
            try {
                new ReceiveServer(Listener.valueOf(args[0]).getPort()).start();
            } catch (IllegalArgumentException e) {
                log.error("error args:{}", args[0]);
            }
        }
    }

    @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> customizer() {
        return factory ->
                factory.addDeploymentInfoCustomizers(deploymentInfo -> {
                    WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
                    webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(false, 1024));
                    deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
                });
    }
}