package org.iii.esd.mongo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

public abstract class AbstractMongoConfig {
    // Mongo DB Properties
    public String host, database, username;
    public char[] password;
    public int port;

    // Setter methods go here..

    /*
     * Method that creates MongoDbFactory
     * Common to both of the MongoDb connections
     */
    public MongoDbFactory mongoDbFactory() throws Exception {
        // return new SimpleMongoDbFactory(new MongoClient(host, port), database);
        ServerAddress serverAddress = new ServerAddress(host, port);
        return new SimpleMongoDbFactory(
                new MongoClient(serverAddress,
                        MongoCredential.createCredential(
                                username, "admin", password),
                        new MongoClientOptions.Builder().build()),
                database);

    }

    /*
     * Factory method to create the MongoTemplate
     */
    abstract public MongoTemplate getMongoTemplate() throws Exception;

    void msleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {

        }
    }

    public void panic(String msg) {
        for (int i = 0; ; i++) {
            System.err.println("*** PANIC *** " + msg);
            msleep(1000);
        }
    }

    public String getEnv(String... as) {
        String si;
        for (String s : as) {
            si = System.getenv(s);
            if (si != null) {
                return si;
            }
        }
        return null;
    }
}