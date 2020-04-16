package org.example;

import org.example.dao.CrudRepository;
import org.example.dao.CrudRepositoryClient;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.CrudExceptionNotFound;
import org.example.service.connection.JdbcConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTestCrud {

    @Autowired
    private Connection connection;

    @Autowired
    private JdbcConnection jdbcConnection;

    @Test
    void shouldOkCreateRepo() {
        assertDoesNotThrow(() -> {
            new CrudRepositoryClient(connection);
        });
    }

    @Test
    void shouldOkCreateEntry(@Autowired CrudRepository<Long, User> repo) throws CrudException {
        Object key = repo.create(new User("bar", "sadas", "123@123.ru"));
        assertNotNull(key);
    }

    @Test
    void shouldOkCreateEntryInvalidChars(@Autowired CrudRepository<Long, User> repo) throws CrudException {
        Long key = repo.create(new User("><*-+~!)(", "Petrov", "123@123.ru"));
        repo.update(key, new User("><*-+~!)(", "Petrov", "123@123.ru"));
        assertNotNull(key);
    }

    @Test
    void shouldOkAddReadDeleteRead(@Autowired CrudRepository<Long, User> repo) throws CrudException {
        User original = new User("bar", "sadas", "123@123.ru");
        Long key = repo.create(original);
        assertNotNull(key);
        User user = repo.read(key);
        assertEquals(original, user);
        repo.delete(key);
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(key));
    }

    @Test
    void shouldOkAddReadUpdateRead(@Autowired CrudRepository<Long, User> repo) throws CrudException {
        User original = new User("bar", "sadas", "123@123.ru");
        Long key = repo.create(original);
        assertNotNull(key);
        User user = repo.read(key);
        assertEquals(original, user);
        User updated = new User("Vasilisa", "Petrova", "123@asdas.ru");
        repo.update(key, updated);
        User afterUpdate = repo.read(key);
        assertEquals(updated, afterUpdate);
    }

    @Test
    void shouldFailNotExistId(@Autowired CrudRepository<Long, User> repo) throws CrudException {
        assertThrows(CrudExceptionNotFound.class, () -> repo.read(100000L));
        assertThrows(CrudExceptionNotFound.class, () -> repo.update(100000L, new User("1", "2", "asd@ad.ry")));
        assertThrows(CrudExceptionNotFound.class, () -> repo.delete(100000L));
    }

    User createMaliciuosEntity() {
        return new User("1','2','3'); DROP TABLE users;'", "", "");
    }

    @Test
    void maliciousRequestTest(@Autowired CrudRepository<Long, User> repo) throws CrudException {
        Long key = repo.create(new User("Vasya", "Petrov", "asda@asdsa.ru"));
        assertNotNull(key);
        User maliciousEntity = createMaliciuosEntity();
        try {
            repo.create(maliciousEntity);
        } catch (Exception ex) {
            System.out.println("Hacked");
        }
        User entityFromDb = repo.read(key);
        assertNotNull(entityFromDb);
    }
}
