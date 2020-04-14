package org.example.service.connection;

import org.example.entity.Client;
import org.example.entity.Entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class JdbcConnectionPostgre implements JdbcConnection {

    private static final String URL = "jdbc:postgresql://localhost/service_db";

    private static final String USERNAME = "admin";

    private static final String PASSWORD = "pass";

    private static Connection connection;
    @Override
    public Connection getConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
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
