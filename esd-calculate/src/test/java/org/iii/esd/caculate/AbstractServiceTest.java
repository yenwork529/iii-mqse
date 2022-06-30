package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import org.iii.esd.mongo.config.MongoDBConfig;

@TestMethodOrder(OrderAnnotation.class)
@ContextConfiguration(classes = MongoDBConfig.class)
@ConfigurationProperties(prefix = "test")
@PropertySource({
        "classpath:application.yml",
        //	"classpath:application-dti.yml",
        //	"classpath:application-dtidev.yml",
//        "classpath:application-local.yml",
        "classpath:application-test.yml",
})
class AbstractServiceTest {

    static final Locale local = Locale.getDefault();
    static final DateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMdd HH:mm:ss", local);


    String getRamdom(int shift, int point) {
        return new BigDecimal((long) (Math.random() * Math.pow(10, shift + point))).divide(new BigDecimal(Math.pow(10, point))).toString();
    }

}
