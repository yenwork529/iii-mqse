package org.iii.esd.mongo.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
// @Configuration
public class MultiMongoConfiguration {
    // @Value("${spring.data.mongodb.host}")
    private String host;

    // @Value("${spring.data.mongodb.port}")
    private Integer port;

    // @Value("${spring.data.mongodb.database}")
    private String database;

    // @Value("${spring.data.mongodb.username}")
    private String username;

    // @Value("${spring.data.mongodb.password}")
    private char[] password;
}
