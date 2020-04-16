package org.example.dao;

import org.example.entity.User;
import org.example.exception.EntityException;

import java.sql.Connection;

public class CrudRepositoryUser extends AbstractRepository<User> {
    public CrudRepositoryUser(Connection connection) throws EntityException {
        super(connection, User.class);
    }
}
