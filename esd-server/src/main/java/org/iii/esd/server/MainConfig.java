package org.iii.esd.server;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfig {

    @Bean("downloadState")
    public Map<String, Boolean> downloadState() {
        return new ConcurrentHashMap<>();
    }

    @Bean("fileToken")
    public Map<String, File> fileToken() {
        return new ConcurrentHashMap<>();
    }

}
