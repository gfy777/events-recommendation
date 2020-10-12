package db;

import db.firestore.FirestoreConnection;
import db.mysql.MySQLConnection;

public class DBConnectionFactory {
    private static final String DEFAULT_DB = "firestore";

    public static DBConnection getConnection(String db) {
        switch (db) {
            case "mysql":
                return new MySQLConnection();
            case "mongodb":
                //return new MongoDBConnection();
                return null;
            case "firestore":
                return new FirestoreConnection();
            default:
                throw new IllegalArgumentException("Invalid db: " + db);
        }
    }

    public static DBConnection getConnection() {
        return getConnection(DEFAULT_DB);
    }
}
