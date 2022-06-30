package org.iii.esd.client.afc;

import org.iii.esd.mongo.config.MongoDBConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ComponentScan(basePackages = { "org.iii" })
@ContextConfiguration(classes = MongoDBConfig.class)
@ConfigurationProperties(prefix = "test")
@TestPropertySource({
	"classpath:application.yml",
	"classpath:application-develop.yml",
	"classpath:application-local.yml",
	"classpath:application-local9F.yml",
})
public class AbstractServiceTest {

}