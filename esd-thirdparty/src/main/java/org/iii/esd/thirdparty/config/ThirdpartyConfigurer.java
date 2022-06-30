package org.iii.esd.thirdparty.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


@Component
public class ThirdpartyConfigurer {
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {

		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
		List<ClassPathResource> list = new ArrayList<>();
		list.add(new ClassPathResource("config.yml"));
		// list.add(new ClassPathResource("outlook.yml"));
		list.add(new ClassPathResource("gmail.yml"));
		list.add(new ClassPathResource("notify.yml"));
		list.add(new ClassPathResource("phone.yml"));
		list.add(new ClassPathResource("suits.yml"));
//		ClassPathResource afc = new ClassPathResource("afc.yml");
//		if(afc.exists()) {
//			list.add(afc);
//		}
		properties.setResources(list.toArray(new ClassPathResource[]{}));
		configurer.setProperties(Objects.requireNonNull(properties.getObject()));
		return configurer;
	}	

}