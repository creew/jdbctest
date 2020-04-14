package org.example.service.server.method;

import org.eclipse.jetty.io.WriterOutputStream;
import org.example.dao.CrudRepository;
import org.example.entity.Client;
import org.example.exception.CrudException;
import org.example.exception.JsonException;
import org.example.service.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_OK;

public class Post implements HttpMethodRunner {
    @Override
    public Response run(String[] paths, InputStream is, CrudRepository<Long, Client> repository) throws JsonException, CrudException, IOException {
        if (paths.length != 1)
            throw new NumberFormatException();
        long key = Long.parseLong(paths[0]);
        Client client = JsonConverter.parseRequestBody(Client.class, is);
        repository.update(key, client);
        Response response = new Response(HttpURLConnection.HTTP_OK);
        JsonConverter.writeMessage(new WriterOutputStream(response.getBody()), HTTP_MESSAGE_OK);
        return response;
    }
}
