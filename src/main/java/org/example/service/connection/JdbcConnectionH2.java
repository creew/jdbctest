package org.example.service.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionH2 implements JdbcConnection{
    private static final String URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
