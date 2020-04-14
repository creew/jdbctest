package org.example.service.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionPostgre implements JdbcConnection {

    private static final String URL = "jdbc:postgresql://localhost/service_db";

    private static final String USERNAME = "admin";

    private static final String PASSWORD = "pass";

    @Override
    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
