package org.iii.esd.thirdparty.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "notify")
public class NotifyConfig {

    private Line line;
    private String[] emails;
    private String[] phones;
    private Specific specific;

    @Getter
    @Setter
    public static class Line {
        private String clientId;
        private String clientSecret;
        private String token;
    }

    @Getter
    @Setter
    public static class Specific {
        private String rm;
        private String ms;
        private String sw;
    }

}