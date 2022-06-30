package org.iii.esd.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootTest
@ComponentScan(basePackages = { "org.iii" })
@ConfigurationProperties(prefix = "test")
@PropertySource({
	"classpath:application.yml",
})
public class AbstractTest {

}
