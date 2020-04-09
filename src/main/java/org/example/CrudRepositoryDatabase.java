package org.example;

import org.example.entity.Client;
import org.example.entity.Entity;
import org.example.exceptions.CrudException;
import org.example.exceptions.CrudExceptionNotFound;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;

public class CrudRepositoryDatabase implements CrudRepository<Long, Client> {
    private static final String SQL_ERROR = "Sql error: ";

    private Connection connection;

    private Map<String, String> map;

    private String table;

    public CrudRepositoryDatabase(Connection connection) {
        this.connection = connection;
        map = Entity.getNamesAndField(Client.class);
        table = Entity.getTableName(Client.class);
    }

    @Override
    public Long create(@NotNull Client object) throws CrudException {
        try (Statement statement = connection.createStatement()) {
            String keys = String.join(", ", map.keySet());
            String values = map.values().stream().map(value -> {
                try {
                    return "'" + object.getValue(value).toString() + "'";
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.joining(", "));
            String sqlCreate = "INSERT INTO " + table + "  (" + keys + ") "
                    + "  VALUES (" + values + ")";
            if (statement.executeUpdate(sqlCreate, Statement.RETURN_GENERATED_KEYS) == 0)
                throw new CrudException("No changes in repository");
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new CrudException("Creating entity failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public Client read(@NotNull Long key) throws CrudException {
        String keys = String.join(", ", map.keySet());
        try (Statement statement = connection.createStatement()) {
            String sqlSelect = "SELECT " + keys + " FROM " + table
                    + " WHERE id = " + key;
            try (ResultSet rs = statement.executeQuery(sqlSelect)) {
                if (rs.next()) {
                    Client client = new Client();
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        client.setValue(entry.getValue(), rs.getObject(entry.getKey()));
                    }
                    return client;
                }
                throw new CrudExceptionNotFound();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new CrudException("Reflection error: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public void update(@NotNull Long key, @NotNull Client value) throws CrudException {
        String setString = map.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + "'" + value.getValue(entry.getValue()) + "'";
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.joining(", "));
        String sqlUpdate = "UPDATE " + table + " SET " + setString + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlUpdate)) {
            statement.setLong(1, key);
            if (statement.executeUpdate() == 0) {
                throw new CrudExceptionNotFound();
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public void delete(@NotNull Long key) throws CrudException {
        try (Statement statement = connection.createStatement()) {
            String sqlDelete = "DELETE FROM " + table
                    + " WHERE id = " + key;
            if (statement.executeUpdate(sqlDelete) == 0) {
                throw new CrudExceptionNotFound();
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }


}
