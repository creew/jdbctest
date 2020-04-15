package org.example;

import org.example.exceptions.CrudExceptionNotFound;
import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.entity.Entity;
import org.example.exceptions.CrudException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestExample {

    private static final String DB = "test";
    private static final String TABLE = "clients";
    private static final String URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private Connection connection;

    @BeforeEach
    public void before() throws SQLException {
        DeleteDbFiles.execute("~", DB, true);
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    @AfterEach
    public void after() throws SQLException {
        connection.close();
    }

    @Test
    void shouldOkCreateRepo() {
        assertDoesNotThrow(() -> {
            new CrudRepositoryDatabase(connection, TABLE);
        });
    }

    @Test
    void shouldOkCreateEntry() throws CrudException {
        CrudRepository<Long, Entity> repo = new CrudRepositoryDatabase(connection, TABLE);
        Long key = repo.create(new Entity("Vasya", "Petrov"));
        assertNotNull(key);
    }

    @Test
    void shouldOkCreateEntryInvalidChars() throws CrudException {
        CrudRepository<Long, Entity> repo = new CrudRepositoryDatabase(connection, TABLE);
        Long key = repo.create(new Entity("><*-+~!)(", "Petrov"));
        repo.update(key, new Entity("><*-+~!)(", "Petrov"));
        assertNotNull(key);
    }

    @Test
    void shouldOkAddReadDeleteRead() throws CrudException {
        CrudRepository<Long, Entity> repo = new CrudRepositoryDatabase(connection, TABLE);
        Entity original = new Entity("Vasya", "Petrov");
        Long key = repo.create(original);
        assertNotNull(key);
        Entity entity = repo.read(key);
        assertEquals(original, entity);
        repo.delete(key);
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(key));
    }

    @Test
    void shouldOkAddReadUpdateRead() throws CrudException {
        CrudRepository<Long, Entity> repo = new CrudRepositoryDatabase(connection, TABLE);
        Entity original = new Entity("Vasya", "Petrov");
        Long key = repo.create(original);
        assertNotNull(key);
        assertEquals(1L, key.longValue());
        Entity entity = repo.read(key);
        assertEquals(original, entity);
        Entity updated = new Entity("Vasilisa", "Petrova");
        repo.update(key, updated);
        Entity afterUpdate = repo.read(key);
        assertEquals(updated, afterUpdate);
    }

    @Test
    void shouldFailNotExistId() throws CrudException {
        CrudRepository<Long, Entity> repo = new CrudRepositoryDatabase(connection, TABLE);
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(100L));
        assertThrows(CrudExceptionNotFound.class, () -> repo.update(100L, new Entity("1", "2")));
        assertThrows(CrudExceptionNotFound.class, () -> repo.delete(100L));
    }

    Entity createMaliciuosEntity() {
        return new Entity("1','2'); DROP TABLE clients;'", "");
    }

    @Test
    void maliciousRequestTest() throws CrudException {
        CrudRepository<Long, Entity> repo = new CrudRepositoryDatabase(connection, TABLE);
        Long key = repo.create(new Entity("Vasya", "Petrov"));
        assertNotNull(key);
        Entity maliciousEntity = createMaliciuosEntity();
        try {
            repo.create(maliciousEntity);
        } catch (Exception ex) {
            System.out.println("Hacked");
        }
        Entity entityFromDb = repo.read(key);
        assertNotNull(entityFromDb);
    }
}
