package org.iii.esd.battery;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


@Component
public class BatteryConfigurer {
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
		//properties.setDocumentMatchers(new SpringProfileDocumentMatcher(profile));
		properties.setResources(
				new ClassPathResource("config/aec.yml"),
				new ClassPathResource("config/chem.yml"),
				new ClassPathResource("config/joseph.yml"),
				new ClassPathResource("config/sungrow.yml")
		);
		configurer.setProperties(properties.getObject());
		return configurer;
	}	

}
