package org.example.service.server.method;

import org.example.dao.CrudRepository;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.JsonException;
import org.example.service.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;

public class Get implements HttpMethodRunner {
    @Override
    public Response run(String[] paths, InputStream is, CrudRepository<Long, User> repository) throws JsonException, CrudException, IOException {
        if (paths.length != 1)
            throw new NumberFormatException();
        User user = repository.read(Long.parseLong(paths[0]));
        try (Writer writer = JsonConverter.writeClient(user)) {
            return new Response(HttpURLConnection.HTTP_OK, writer);
        }
    }
}
