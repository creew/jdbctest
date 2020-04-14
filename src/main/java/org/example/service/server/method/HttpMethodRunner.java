package org.example.service.server.method;

import org.example.dao.CrudRepository;
import org.example.entity.Client;
import org.example.exception.CrudException;
import org.example.exception.JsonException;

import java.io.IOException;
import java.io.InputStream;

public interface HttpMethodRunner{

    Response run(String[] paths, InputStream is, CrudRepository<Long, Client> repository) throws JsonException, CrudException, IOException;

}
