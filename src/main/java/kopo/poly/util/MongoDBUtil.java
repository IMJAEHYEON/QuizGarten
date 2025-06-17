package kopo.poly.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import kopo.poly.config.MongoDBConfig;
import org.springframework.stereotype.Component;

@Component
public class MongoDBUtil {

    private final MongoClient mongoClient;
    private final MongoDBConfig mongoDBConfig;

    public MongoDBUtil(MongoDBConfig mongoDBConfig) {
        this.mongoDBConfig = mongoDBConfig;
        this.mongoClient = MongoClients.create(mongoDBConfig.getConnectionUri());
    }

    public MongoDatabase getMongoDB(String dbName) {
        return mongoClient.getDatabase(dbName);
    }
}
