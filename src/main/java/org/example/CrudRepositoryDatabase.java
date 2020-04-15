package org.example;

import org.example.exceptions.CrudException;
import org.example.exceptions.CrudExceptionNotFound;
import org.jetbrains.annotations.NotNull;
import org.example.entity.Entity;

import java.sql.*;

public class CrudRepositoryDatabase implements CrudRepository<Long, Entity> {
    private static final String SQL_ERROR = "Sql error: ";

    private final Connection connection;

    private final String table;

    public CrudRepositoryDatabase(Connection connection, String table) throws CrudException {
        this.connection = connection;
        this.table = table;
        try (Statement statement = this.connection.createStatement()) {
            String sqlCreate = "CREATE TABLE IF NOT EXISTS " + this.table
                    + "  (first_name      VARCHAR(50),"
                    + "   last_name       VARCHAR(50),"
                    + "   id              INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT)";
            statement.execute(sqlCreate);
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public Long create(@NotNull Entity object) throws CrudException {
        String sqlCreate = "INSERT INTO " + table
                + "  (first_name, last_name) "
                + "  VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sqlCreate, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, object.getFirstName());
            statement.setObject(2, object.getLastName());
            if (statement.executeUpdate() == 0)
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
    public Entity read(@NotNull Long key) throws CrudException {
        String sqlSelect = "SELECT first_name, last_name FROM " + table
                + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlSelect)) {
            statement.setObject(1, key);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    return new Entity(firstName, lastName);
                }
                throw new CrudExceptionNotFound();
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }

    @Override
    public void update(@NotNull Long key, @NotNull Entity value) throws CrudException {
        String sqlUpdate = "UPDATE " + table + " SET first_name=?, last_name=? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlUpdate)) {
            statement.setString(1, value.getFirstName());
            statement.setString(2, value.getLastName());
            statement.setLong(3, key);
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
                + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlDelete)) {
            statement.setObject(1, key);
            if (statement.executeUpdate() == 0) {
                throw new CrudExceptionNotFound();
            }
        } catch (SQLException e) {
            throw new CrudException(SQL_ERROR + e.getMessage());
        }
    }
}
