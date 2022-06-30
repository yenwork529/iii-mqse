package org.iii.esd.mongo.config;


import java.util.Arrays;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import org.iii.esd.mongo.event.CascadeSaveMongoEventListener;

@Configuration
@RequiredArgsConstructor
@EnableMongoRepositories(basePackages = "org.iii.esd.mongo")
@EnableMongoAuditing
public class MongoDBConfig {

    private final MongoProperties mongoProperties;
    @Autowired
    private MongoMappingContext mongoMappingContext;

    @Bean
    public CascadeSaveMongoEventListener cascadingMongoEventListener() {
        return new CascadeSaveMongoEventListener();
    }

    @Primary
    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(getPrimaryFactory(), getMappingMongoConverter());
    }

    @Bean(name = "secondaryTemplate")
    public MongoTemplate getMongoTemplate() throws Exception {
        return new MongoTemplate(getSecondaryFactory());
    }

    private MappingMongoConverter getMappingMongoConverter() throws Exception {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(getPrimaryFactory());
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        // to remove "_class" attribute when adding in mongodb
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.setCustomConversions(
                new MongoCustomConversions(Arrays.asList(new DoubleToBigDecimalConverter(), new BigDecimalToDoubleConverter())));
        return converter;
    }

    private MongoDbFactory getPrimaryFactory() throws Exception {
        ServerAddress serverAddress = new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort());
        return new SimpleMongoDbFactory(
                mongoProperties.getUsername() != null ?
                        new MongoClient(serverAddress,
                                MongoCredential.createCredential(mongoProperties.getUsername(), "admin", mongoProperties.getPassword()),
                                new MongoClientOptions.Builder().build()) :
                        new MongoClient(serverAddress)
                , mongoProperties.getDatabase());
    }

    private MongoDbFactory getSecondaryFactory() throws Exception {
        ServerAddress serverAddress = new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort());
        return new SimpleMongoDbFactory(
                mongoProperties.getUsername() != null ?
                        new MongoClient(serverAddress,
                                MongoCredential.createCredential(mongoProperties.getUsername(), "admin", mongoProperties.getPassword()),
                                new MongoClientOptions.Builder().build()) :
                        new MongoClient(serverAddress)
                , getSecondaryDatabaseName(mongoProperties.getDatabase()));
    }

    private String getSecondaryDatabaseName(String primaryDatabase){
        return String.format("%s_T", primaryDatabase);
    }

}