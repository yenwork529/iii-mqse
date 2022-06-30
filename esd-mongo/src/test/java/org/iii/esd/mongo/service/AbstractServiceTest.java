package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Locale;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import org.iii.esd.Constants;
import org.iii.esd.mongo.config.MongoDBConfig;

@TestMethodOrder(OrderAnnotation.class)
@ComponentScan(basePackages = {"org.iii"})
@ConfigurationProperties(prefix = "test")
@ContextConfiguration(classes = MongoDBConfig.class,
        initializers = AbstractServiceTest.Initializer.class)
@TestPropertySource({
        "classpath:application.yml"
})
public class AbstractServiceTest {

    static final Locale local = Locale.getDefault();
    static final DateFormat yyyyMMddHHmmss = Constants.TIMESTAMP_FORMAT2;

    String getRamdom(int shift, int point) {
        return new BigDecimal((long) (Math.random() * Math.pow(10, shift + point))).divide(new BigDecimal(Math.pow(10, point))).toString();
    }

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