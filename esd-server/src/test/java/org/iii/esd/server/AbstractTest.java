package org.iii.esd.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

@ComponentScan(basePackages = {"org.iii"})
@ConfigurationProperties(prefix = "test")
@ContextConfiguration(initializers = AbstractTest.Initializer.class)
@TestPropertySource({
        "classpath:application.yml"
})
public class AbstractTest {

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertySourceUtils.addPropertiesFilesToEnvironment(
                    context,
                    String.format("classpath:/application-%s.yml", context.getEnvironment().getActiveProfiles()[0])
            );
        }
    }

}