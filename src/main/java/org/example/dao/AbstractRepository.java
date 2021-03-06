package org.example.dao;

import org.example.entity.Entities;
import org.example.entity.Entity;
import org.example.exception.CrudException;
import org.example.exception.CrudExceptionNotFound;
import org.example.exception.EntityException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractRepository<V extends Entity> implements CrudRepository<Long, V> {

    private static final String SQL_ERROR = "Sql error: ";

    private Connection connection;

    private Map<String, String> namesAndFields;

    private String table;

    private String id;

    private Class<V> clazz;

    public AbstractRepository(Connection connection, Class<V> clazz) throws EntityException {
        this.connection = connection;
        this.clazz = clazz;
        namesAndFields = Entities.getNamesAndFields(clazz);
        table = Entities.getTableName(clazz);
        id = Entities.getIdColumnName(clazz);
    }

    @Override
    public Long create(@NotNull V object) throws CrudException {
        Map<String, Object> map;
        try {
            map = Entities.getNonNullNamesAndValues(object);
        } catch (EntityException e) {
            throw new CrudException(e.getMessage());
        }
        String sqlCreate = "INSERT INTO " + table + " (" + String.join(", ", map.keySet()) + ") "
                + "VALUES (" + Stream.generate(() -> "?").limit(map.size()).collect(Collectors.joining(", "))
                + ") RETURNING " + id;
        try (PreparedStatement statement = connection.prepareStatement(sqlCreate, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (Object o : map.values()) {
                statement.setObject(index++, o);
            }
            if (statement.executeUpdate() == 0) {
                throw new CrudException("No changes in repository");
            }
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

    private V executeSelect(PreparedStatement statement) throws CrudException, SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                V client = clazz.getDeclaredConstructor().newInstance();
                for (Map.Entry<String, String> entry : namesAndFields.entrySet()) {
                    client.setValue(entry.getValue(), rs.getObject(entry.getKey()));
                }
                return client;
            }
            throw new CrudExceptionNotFound();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new CrudException("Reflection error: " + e.getMessage());
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new CrudException("Reflection error create instance: " + e.getMessage());
        }
    }

    @Override
    public V read(@NotNull Long key) throws CrudException {
        String sqlSelect = "SELECT " + String.join(", ", namesAndFields.keySet()) + " FROM " + table
                + " WHERE " + id + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlSelect)) {
            statement.setObject(1, key);
            return executeSelect(statement);
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public void update(@NotNull Long key, @NotNull V value) throws CrudException {
        Map<String, Object> nonNull;
        try {
            nonNull = Entities.getNonNullNamesAndValues(value);
        } catch (EntityException e) {
            throw new CrudException(e.getMessage());
        }
        String setString = nonNull.keySet().stream().map(s -> (s + "=?")).collect(Collectors.joining(", "));
        String sqlUpdate = "UPDATE " + table + " SET " + setString + " WHERE " + id + "=?";
        try (PreparedStatement statement = connection.prepareStatement(sqlUpdate)) {
            int index = 1;
            for (Object o : nonNull.values()) {
                statement.setObject(index++, o);
            }
            statement.setObject(index, key);
            if (statement.executeUpdate() == 0) {
                throw new CrudExceptionNotFound();
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public void delete(@NotNull Long key) throws CrudException {
        String sqlDelete = "DELETE FROM " + table
                + " WHERE " + id + " = " + key;
        try (PreparedStatement statement = connection.prepareStatement(sqlDelete)) {
            if (statement.executeUpdate() == 0) {
                throw new CrudExceptionNotFound();
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }
}
