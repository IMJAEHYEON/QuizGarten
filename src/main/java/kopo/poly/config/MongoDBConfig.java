package kopo.poly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    public String getConnectionUri() {
        if (username == null || username.isEmpty()) {
            return String.format("mongodb://%s:%d", host, port);
        }
        return String.format("mongodb://%s:%s@%s:%d/?authSource=%s", username, password, host, port, database);
    }

}
