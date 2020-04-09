package org.example.service;

import java.sql.Connection;

public interface JdbcConnection {
    Connection getConnection();
}
