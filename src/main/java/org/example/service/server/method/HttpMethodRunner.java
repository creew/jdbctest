package org.example.service.server.method;

import org.example.dao.CrudRepository;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.JsonException;

import java.io.IOException;
import java.io.InputStream;

public interface HttpMethodRunner{

    Response run(String[] paths, InputStream is, CrudRepository<Long, User> repository) throws JsonException, CrudException, IOException;

}
