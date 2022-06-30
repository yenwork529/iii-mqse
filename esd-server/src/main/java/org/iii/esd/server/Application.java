package org.iii.esd.server;

import io.undertow.UndertowOptions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.iii.esd.enums.Listener;
import org.iii.esd.thirdparty.socket.ReceiveServer;

@ComponentScan(basePackages = {"org.iii.esd"})
@SpringBootApplication
@ServletComponentScan
@Log4j2
@EnableAsync
public class Application {

    @Value("${server.port}")
    private Integer port;

    private Integer httpPort = 58003;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // log.info("monitor port:{}", Listener.SERVER.getPort());
        // new ReceiveServer(Listener.SERVER.getPort()).start();
    }

    // @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> containerCustomizer() {
        return (WebServerFactoryCustomizer<UndertowServletWebServerFactory>) factory -> {
            UndertowServletWebServerFactory undertowFactory = (UndertowServletWebServerFactory) factory;
            undertowFactory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true));
            undertowFactory.getBuilderCustomizers().add(builder -> {
                builder.addHttpListener(httpPort, "127.0.0.1");
                //builder.addHttpListener(httpPort, "0.0.0.0");
            });
            //	        // http 8000 redirect https 8001
            //	        undertowFactory.addDeploymentInfoCustomizers(deploymentInfo -> {
            //				deploymentInfo
            //						.addSecurityConstraint(new SecurityConstraint()
            //								.addWebResourceCollection(new WebResourceCollection().addUrlPattern("/*").addHttpMethods("POST","PUT","GET"))
            //								.setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
            //								.setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
            //						.setConfidentialPortManager(exchange -> port);
            //			});
        };
    }


    //	@Bean
    //	public ServletWebServerFactory servletContainer() {
    //		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
    //		tomcat.addAdditionalTomcatConnectors(createSslConnector());
    //		return tomcat;
    //	}
    //
    //	private Connector createSslConnector() {
    //		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    //		Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
    //		try {
    //			File keystore = new ClassPathResource("dtiesd.p12").getFile();
    //			connector.setScheme("http");
    //			connector.setPort(httpPort);
    //			connector.setSecure(false);
    //			// http 8000 redirect https 8001
    //			connector.setRedirectPort(port);
    //			protocol.setSSLEnabled(true);
    //			protocol.setKeystoreFile(keystore.getAbsolutePath());
    //			protocol.setKeystorePass(key_store_password);
    //			protocol.setKeyPass(key_password);
    //			return connector;
    //		} catch (IOException ex) {
    //			throw new IllegalStateException(
    //					"can't access keystore: [" + "keystore" + "] or truststore: [" + "keystore" + "]", ex);
    //		}
    //	}

}