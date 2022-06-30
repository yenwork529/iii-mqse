package org.iii.esd.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "config")
public class MonitorConfig {

    private Map<String, String> monitor;

}