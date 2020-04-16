package org.example.service.server.method;

import org.eclipse.jetty.io.WriterOutputStream;
import org.example.dao.CrudRepository;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.service.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;

import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_OK;

public class Delete implements HttpMethodRunner {
    @Override
    public Response run(String[] paths, InputStream is, CrudRepository<Long, User> repository) throws CrudException, IOException {
        if (paths.length != 1)
            throw new NumberFormatException();
        repository.delete(Long.parseLong(paths[0]));
        try (Writer writer = new StringWriter()) {
            JsonConverter.writeMessage(new WriterOutputStream(writer), HTTP_MESSAGE_OK);
            return new Response(HttpURLConnection.HTTP_NO_CONTENT);
        }
    }
}
