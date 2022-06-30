package org.iii.esd.client.afc;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@ComponentScan(basePackages = { "org.iii.esd" })
@EnableCaching
@SpringBootApplication
@ServletComponentScan
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
		properties.setResources(
				new ClassPathResource("config/sungrow.yml"),
				new ClassPathResource("suits.yml"),
				new ClassPathResource("afc.yml")
		);
		configurer.setProperties(properties.getObject());
		return configurer;
	}
	
}