package org.example.dao;

import org.example.entity.Client;
import org.example.exception.EntityException;

import java.sql.Connection;

public class CrudRepositoryClient extends AbstractRepository<Client> {
    public CrudRepositoryClient(Connection connection) throws EntityException {
        super(connection, Client.class);
    }
}
