package org.example.dao;

import org.example.entity.Client;
import org.example.exception.EntityException;

import java.sql.Connection;

public class CrudRepositoryClient extends AbstractRepository<Long, Client> {
    public CrudRepositoryClient(Connection connection) throws EntityException {
        super(connection);
        super.init(Client.class);
    }
}
