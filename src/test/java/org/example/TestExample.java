package org.example;

import org.example.entity.Client;
import org.example.exceptions.CrudException;
import org.example.exceptions.CrudExceptionNotFound;
import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("/springContext.xml")
public class TestExample {

    private static final String DB = "test";

    @Autowired
    Connection connection;

    @BeforeEach
    public void before() throws SQLException {
        DeleteDbFiles.execute("~", DB, true);
        try (Statement statement = connection.createStatement()) {
            String params = Client.getNamesAndTypes(Client.class).entrySet().stream()
                    .map(entry -> entry.getKey() + " " + entry.getValue())
                    .collect(Collectors.joining(", "));
            String sqlCreate = "CREATE TABLE IF NOT EXISTS " + Client.getTableName(Client.class)
                    + "  (" + params + ")";
            statement.execute(sqlCreate);
        }
    }

    @Test
    void shouldOkCreateRepo() {
        assertDoesNotThrow(() -> {
            new CrudRepositoryDatabase(connection);
        });
    }

    @Test
    void shouldOkCreateEntry(@Autowired CrudRepository<Long, Client> repo) throws CrudException {
        Long key = repo.create(new Client("Vasya", "Petrov"));
        assertNotNull(key);
    }

    @Test
    void shouldOkCreateEntryInvalidChars(@Autowired CrudRepository<Long, Client> repo) throws CrudException {
        Long key = repo.create(new Client("><*-+~!)(", "Petrov"));
        repo.update(key, new Client("><*-+~!)(", "Petrov"));
        assertNotNull(key);
    }

    @Test
    void shouldOkAddReadDeleteRead(@Autowired CrudRepository<Long, Client> repo) throws CrudException {
        Client original = new Client("Vasya", "Petrov");
        Long key = repo.create(original);
        assertNotNull(key);
        Client client = repo.read(key);
        assertEquals(original, client);
        repo.delete(key);
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(key));
    }

    @Test
    void shouldOkAddReadUpdateRead(@Autowired CrudRepository<Long, Client> repo) throws CrudException {
        Client original = new Client("Vasya", "Petrov");
        Long key = repo.create(original);
        assertNotNull(key);
        Client client = repo.read(key);
        assertEquals(original, client);
        Client updated = new Client("Vasilisa", "Petrova");
        repo.update(key, updated);
        Client afterUpdate = repo.read(key);
        assertEquals(updated, afterUpdate);
    }

    @Test
    void shouldFailNotExistId(@Autowired CrudRepository<Long, Client> repo) throws CrudException {
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(100L));
        assertThrows(CrudExceptionNotFound.class, () -> repo.update(100L, new Client("1", "2")));
        assertThrows(CrudExceptionNotFound.class, () -> repo.delete(100L));
    }
}
