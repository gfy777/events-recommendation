package db.mysql;


import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

// help reset database
public class MySQLTableCreation {

    public static void main(String[] args) {
        try {
            Connection connection = null;

            // step 1 - connection to DB
            try {
                System.out.println("Connecting to " + MySQLDBUtil.URL);
                // Loading class `com.mysql.jdbc.Driver'.
                 Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(MySQLDBUtil.URL);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (connection == null) {
                return ;
            }

            // step 2 - Drop tables in case exists
            Statement statement = connection.createStatement();
            String sql = "DROP TABLE IF EXISTS categories";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS history";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);


            // step 3 - create tables
            sql = "CREATE TABLE items "
                    + "(item_id VARCHAR(255) NOT NULL,"
                    + " name VARCHAR(255),"
                    + " rating FLOAT,"
                    + " address VARCHAR(255),"
                    + " image_url VARCHAR(255),"
                    + " url VARCHAR(255),"
                    + " distance FLOAT,"
                    + " PRIMARY KEY (item_id))";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE categories "
                    + "(item_id VARCHAR(255) NOT NULL,"
                    + " category VARCHAR(255) NOT NULL,"
                    + " PRIMARY KEY (item_id, category),"
                    + " FOREIGN KEY (item_id) REFERENCES items(item_id))";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users "
                    + "(user_id VARCHAR(255) NOT NULL,"
                    + " password VARCHAR(255) NOT NULL,"
                    + " first_name VARCHAR(255),"
                    + " last_name VARCHAR(255),"
                    + "PRIMARY KEY (user_id))";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE history "
                    + "(user_id VARCHAR(255) NOT NULL,"
                    + " item_id VARCHAR(255) NOT NULL,"
                    + " last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,"
                    + " PRIMARY KEY (user_id, item_id),"
                    + " FOREIGN KEY (item_id) REFERENCES items(item_id),"
                    + " FOREIGN KEY (user_id) REFERENCES users(user_id))";
            statement.executeUpdate(sql);

            System.out.println("Clean up successfully");

            // step 4 - insert fake user
            String salt = BCrypt.gensalt();
            String fakeUserPassword = BCrypt.hashpw("ryan", salt);
            sql = "INSERT INTO users (user_id, password) VALUES ('ryan', ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, fakeUserPassword);

            preparedStatement.executeUpdate();



        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
