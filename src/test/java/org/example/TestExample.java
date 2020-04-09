package org.example;

import org.example.entity.Client;
import org.example.exceptions.CrudException;
import org.example.exceptions.CrudExceptionNotFound;
import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestExample {

    private static final String DB = "test";
    private static final String URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private Connection connection;

    @BeforeEach
    public void before() throws SQLException {
        DeleteDbFiles.execute("~", DB, true);
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        try (Statement statement = connection.createStatement()) {
            String params = Client.getNamesAndTypes(Client.class).entrySet().stream()
                    .map(entry -> entry.getKey() + " " + entry.getValue())
                    .collect(Collectors.joining(", "));
            String sqlCreate = "CREATE TABLE IF NOT EXISTS " + Client.getTableName(Client.class)
                    + "  (" + params + ")";
            statement.execute(sqlCreate);
        }
    }

    @AfterEach
    public void after() throws SQLException {
        connection.close();
    }

    @Test
    void shouldOkCreateRepo() {
        assertDoesNotThrow(() -> {
            new CrudRepositoryDatabase(connection);
        });
    }

    @Test
    void shouldOkCreateEntry() throws CrudException {
        CrudRepository<Long, Client> repo = new CrudRepositoryDatabase(connection);
        Long key = repo.create(new Client("Vasya", "Petrov"));
        assertNotNull(key);
    }

    @Test
    void shouldOkCreateEntryInvalidChars() throws CrudException {
        CrudRepository<Long, Client> repo = new CrudRepositoryDatabase(connection);
        Long key = repo.create(new Client("><*-+~!)(", "Petrov"));
        repo.update(key, new Client("><*-+~!)(", "Petrov"));
        assertNotNull(key);
    }

    @Test
    void shouldOkAddReadDeleteRead() throws CrudException {
        CrudRepository<Long, Client> repo = new CrudRepositoryDatabase(connection);
        Client original = new Client("Vasya", "Petrov");
        Long key = repo.create(original);
        assertNotNull(key);
        Client client = repo.read(key);
        assertEquals(original, client);
        repo.delete(key);
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(key));
    }

    @Test
    void shouldOkAddReadUpdateRead() throws CrudException {
        CrudRepository<Long, Client> repo = new CrudRepositoryDatabase(connection);
        Client original = new Client("Vasya", "Petrov");
        Long key = repo.create(original);
        assertNotNull(key);
        assertEquals(1L, key.longValue());
        Client client = repo.read(key);
        assertEquals(original, client);
        Client updated = new Client("Vasilisa", "Petrova");
        repo.update(key, updated);
        Client afterUpdate = repo.read(key);
        assertEquals(updated, afterUpdate);
    }

    @Test
    void shouldFailNotExistId() throws CrudException {
        CrudRepository<Long, Client> repo = new CrudRepositoryDatabase(connection);
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(100L));
        assertThrows(CrudExceptionNotFound.class, () -> repo.update(100L, new Client("1", "2")));
        assertThrows(CrudExceptionNotFound.class, () -> repo.delete(100L));
    }
}
