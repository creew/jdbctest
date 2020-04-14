package org.example;

import org.example.dao.CrudRepository;
import org.example.dao.CrudRepositoryClient;
import org.example.entity.Client;
import org.example.exception.CrudException;
import org.example.exception.CrudExceptionNotFound;
import org.example.service.connection.JdbcConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("/testSpringContext.xml")
public class TestExample {

    private static final String DB = "test";

    @Autowired
    Connection connection;

    @Autowired
    JdbcConnection jdbcConnection;

    @Test
    void shouldOkCreateRepo() {
        assertDoesNotThrow(() -> {
            new CrudRepositoryClient(connection);
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
