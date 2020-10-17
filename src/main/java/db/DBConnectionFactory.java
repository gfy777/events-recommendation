package db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import db.firestore.FirestoreConnection;
import db.mysql.MySQLConnection;
import db.mysql.MySQLDBUtil;
import db.postgresql.PostgreSqlConnection;
import db.postgresql.PostgreSqlDBUtil;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

public class DBConnectionFactory {
    private static final String DEFAULT_DB = "postgresql";

    private static ComboPooledDataSource cpds;

    private DBConnectionFactory() {

    }

    public static DBConnection getConnection(String db) throws PropertyVetoException, SQLException {
        if (cpds == null) {
            cpds = new ComboPooledDataSource();

            switch (db) {
                case "mysql":
                    cpds.setDriverClass("com.mysql.jdbc.Driver");
                    cpds.setJdbcUrl(MySQLDBUtil.URL);
                    break;
                case "postgresql":
                    cpds.setDriverClass("org.postgresql.Driver");
                    cpds.setJdbcUrl(PostgreSqlDBUtil.URL);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid db: " + db);
            }

            // the settings below are optional -- c3p0 can work with defaults
            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(20);
            cpds.setMaxStatements(180);
        }

        switch (db) {
            case "mysql":
                return new MySQLConnection(cpds.getConnection());
            case "firestore":
                return new FirestoreConnection();
            case "postgresql":
                return new PostgreSqlConnection(cpds.getConnection());
            default:
                throw new IllegalArgumentException("Invalid db: " + db);
        }
    }

    public static DBConnection getConnection() {
        try {
            return getConnection(DEFAULT_DB);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
