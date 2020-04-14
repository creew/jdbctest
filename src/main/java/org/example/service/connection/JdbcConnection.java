package org.example.service.connection;

import java.sql.Connection;

public interface JdbcConnection {

    Connection getConnection();

}
