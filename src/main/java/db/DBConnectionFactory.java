package db;

import db.firestore.FirestoreConnection;
import db.mysql.MySQLConnection;
import db.postgresql.PostgreSqlConnection;

public class DBConnectionFactory {
    private static final String DEFAULT_DB = "postgresql";

    public static DBConnection getConnection(String db) {
        switch (db) {
            case "mysql":
                return new MySQLConnection();
            case "mongodb":
                //return new MongoDBConnection();
                return null;
            case "firestore":
                return new FirestoreConnection();
            case "postgresql":
                return new PostgreSqlConnection();
            default:
                throw new IllegalArgumentException("Invalid db: " + db);
        }
    }

    public static DBConnection getConnection() {
        return getConnection(DEFAULT_DB);
    }
}
