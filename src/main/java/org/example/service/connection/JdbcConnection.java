package org.example.service.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnection {
    Connection getConnection();

    void init() throws SQLException;
}
