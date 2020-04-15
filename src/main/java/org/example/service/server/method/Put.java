package org.example.service.server.method;

import org.eclipse.jetty.io.WriterOutputStream;
import org.example.dao.CrudRepository;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.JsonException;
import org.example.service.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;

import static org.example.service.JsonConverter.writeMessage;
import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_OK;

public class Put implements HttpMethodRunner {
    @Override
    public Response run(String[] paths, InputStream is, CrudRepository<Long, User> repository) throws JsonException, CrudException, IOException {
        if (paths.length != 0)
            throw new NumberFormatException();
        User user = JsonConverter.parseRequestBody(User.class, is);
        Object id = repository.create(user);
        try (Writer writer = new StringWriter()) {
            writeMessage(new WriterOutputStream(writer), HTTP_MESSAGE_OK, "ID", id.toString());
            return new Response(HttpURLConnection.HTTP_CREATED, writer);
        }
    }
}
