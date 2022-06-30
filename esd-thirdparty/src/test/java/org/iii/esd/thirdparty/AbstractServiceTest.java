package org.iii.esd.thirdparty;


import org.iii.esd.thirdparty.config.Config;
import org.iii.esd.thirdparty.config.ThirdpartyConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import freemarker.template.Configuration;

@ComponentScan(basePackages = { "org.iii" })
@ContextConfiguration(
		classes = {Config.class, Configuration.class}, 
		loader = AnnotationConfigContextLoader.class)
@ConfigurationProperties(prefix = "test")
@TestPropertySource({"classpath:application.yml"})
//@EnableConfigurationProperties
@Import(ThirdpartyConfigurer.class)
public class AbstractServiceTest {

}
