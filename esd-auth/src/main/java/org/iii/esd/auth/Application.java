package org.iii.esd.auth;

import org.iii.esd.enums.Listener;
import org.iii.esd.thirdparty.socket.ReceiveServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "org.iii.esd" })
@SpringBootApplication
@ServletComponentScan
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		new ReceiveServer(Listener.AUTH.getPort()).start();
	}

}