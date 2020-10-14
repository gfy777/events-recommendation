package db.postgresql;

public class PostgreSqlDBUtil {
    private static final String HOSTNAME = "localhost";
    private static final String PORT_NUM = "5432";
    public static final String DB_NAME = "events_recommendation_project";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    public static final String URL = "jdbc:postgresql://" + HOSTNAME + ":"
            + PORT_NUM + "/" + DB_NAME + "?user=" + USERNAME + "&password="
            + PASSWORD + "&autoReconnect=true&serverTimezone=UTC";
}
