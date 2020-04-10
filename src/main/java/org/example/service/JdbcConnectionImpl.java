package org.example.service;

import org.example.entity.Client;
import org.example.entity.Entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class JdbcConnectionImpl implements JdbcConnection{
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
        try {
            init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public void init() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String params = Entity.getNamesAndTypes(Client.class).entrySet().stream()
                    .map(entry -> entry.getKey() + " " + entry.getValue())
                    .collect(Collectors.joining(", "));
            String sqlCreate = "CREATE TABLE IF NOT EXISTS " + Entity.getTableName(Client.class)
                    + "  (" + params + ")";
            statement.execute(sqlCreate);
        }
    }
}
