package org.iii.esd.mongo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// @Configuration
// @ConfigurationProperties(prefix = "spring.data.primary.mongodb")
// @ConfigurationProperties(prefix = "spring.data.mongodb")
// @EnableMongoRepositories(basePackages = "org.iii.esd.mongo")
// @EnableMongoAuditing
public class PrimaryMongoConfig extends AbstractMongoConfig {

    // @Autowired
    // private final MongoProperties pp;
    // @Value("${spring.ws.host}")
    // String h;

    public PrimaryMongoConfig() {
        String si;
        si = getEnv("ESD_PRI_MONGO_IP", "ESD_MONGO_IP");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.host = si;
        }

        si = getEnv("ESD_PRI_MONGO_DB", "ESD_MONGO_DB");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.database = si;
        }
        // super.database = "ESD_PRI";

        si = getEnv("ESD_PRI_MONGO_USER", "ESD_MONGO_USER");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.username = si;
        }

        si = getEnv("ESD_PRI_MONGO_PASS", "ESD_MONGO_PASS");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.password = si.toCharArray();
        }

        si = getEnv("ESD_PRI_MONGO_PORT", "ESD_MONGO_PORT");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.port = Integer.parseInt(si);
        }
    }

    // @Primary
    @Override
    // @Bean(name = "primaryMongoTemplate")
    public MongoTemplate getMongoTemplate() throws Exception {
        // super.host = pp.getHost();
        if (host == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
            return null;
        }
        return new MongoTemplate(mongoDbFactory());
    }

    // @Bean
    // public MongoClient mongoClient() {
    //     return new MongoClient(host, port);
    // }
}