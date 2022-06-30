package org.iii.esd.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.iii.esd.initializer.Initializer;

@Profile("!test")
@Component
@Slf4j
public class MainRunner implements CommandLineRunner {

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private Initializer initializer;

    @Override
    public void run(String... args) throws Exception {
        log.info("init env {}", env);
        initializer.initData(env);
    }
}
