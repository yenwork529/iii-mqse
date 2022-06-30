package org.iii.esd.mongo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

// @Configuration
// @ConfigurationProperties(prefix = "data.secondary.mongodb")
public class SecondaryMongoConfig extends AbstractMongoConfig {

    public SecondaryMongoConfig(){
        String si;
        si = getEnv("ESD_SEC_MONGO_IP", "ESD_MONGO_IP");
        if (si == null) {
            panic("NULL SEC MONGO CONFIGURATION");
        } else {
            super.host = si;
        }

        si = getEnv("ESD_SEC_MONGO_DB", "ESD_MONGO_DB");
        if (si == null) {
            panic("NULL SEC MONGO CONFIGURATION");
        } else {
            super.database = si;
        }
        super.database = super.database + "_T";

        si = getEnv("ESD_SEC_MONGO_USER", "ESD_MONGO_USER");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.username = si;
        }

        si = getEnv("ESD_SEC_MONGO_PASS", "ESD_MONGO_PASS");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.password = si.toCharArray();
        }

        si = getEnv("ESD_SEC_MONGO_PORT", "ESD_MONGO_PORT");
        if (si == null) {
            panic("NULL PRIMARY MONGO CONFIGURATION");
        } else {
            super.port = Integer.parseInt(si);
        }
    }

    // @Primary
    @Override
    // @Bean(name = "secondaryMongoTemplate")
    public MongoTemplate getMongoTemplate() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }
}