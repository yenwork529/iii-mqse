package org.iii.esd;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.iii.esd.initializer.Initializer;

@SpringBootApplication
public class Application {

    @Autowired
    private Initializer initializer;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }
}
