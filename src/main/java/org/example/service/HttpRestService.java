package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.dao.CrudRepository;
import org.example.entity.Client;
import org.example.exceptions.CrudException;
import org.example.exceptions.JsonException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpRestService implements RestService {

    private static final String CONTEXT = "/v1/client";

    private static final String HTTP_MESSAGE_INVALID_REQUEST = "Invalid request";

    private static final String HTTP_MESSAGE_OK = "Ok";

    private static final String HTTP_MESSAGE_NOT_FOUND = "Not found";

    private CrudRepository<Long, Client> repository;

    private HttpServer server;

    public HttpRestService(CrudRepository<Long, Client> repository, int port) throws IOException {
        this.repository = repository;
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext(CONTEXT, new MyHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    private class MyHttpHandler implements HttpHandler {

        private String[] getPaths(String path) {
            String[] paths = {};
            if (CONTEXT.equalsIgnoreCase(path.substring(0, CONTEXT.length()))) {
                String diff = path.substring(CONTEXT.length());
                if (diff.length() > 0 && diff.charAt(0) == '/') {
                    diff = diff.substring(1);
                }
                if (diff.length() > 0) {
                    paths = diff.split("/");
                }
            }
            return paths;
        }

        private void writeMessage(OutputStream os, String message, String... keyval) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = new HashMap<>();
            map.put("Message", message);
            if (keyval.length > 0 && keyval.length % 2 == 0) {
                for (int i = 0; i < keyval.length; i += 2) {
                    map.put(keyval[i], keyval[i + 1]);
                }
            }
            mapper.writeValue(os, map);
        }

        private Client parseRequestBody(InputStream is) throws JsonException {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(is, Client.class);
            } catch (IOException e) {
                throw new JsonException("Error parse body");
            }
        }

        private Writer writeClient(Client client) throws JsonException {
            StringWriter sw = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(sw, client);
                return sw;
            } catch (IOException e) {
                throw new JsonException("Write class error");
            }
        }

        void runMethod(HttpExchange httpExchange, MethodRunner runner) throws IOException {
            String[] paths = getPaths(httpExchange.getRequestURI().getPath());
            try {
                runner.run(paths);
            } catch (NumberFormatException e) {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                writeMessage(httpExchange.getResponseBody(), HTTP_MESSAGE_INVALID_REQUEST);
            } catch (CrudException e) {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                writeMessage(httpExchange.getResponseBody(), HTTP_MESSAGE_NOT_FOUND);
            } catch (JsonException e) {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                writeMessage(httpExchange.getResponseBody(), e.getMessage());
            }
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    runMethod(httpExchange, paths -> {
                        if (paths.length != 1)
                            throw new NumberFormatException();
                        Client client = repository.read(Long.parseLong(paths[0]));
                        try (Writer writer = writeClient(client)) {
                            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            httpExchange.getResponseBody().write(writer.toString().getBytes());
                        }
                    });
                    break;

                case "POST":
                    runMethod(httpExchange, paths -> {
                        if (paths.length != 1)
                            throw new NumberFormatException();
                        long key = Long.parseLong(paths[0]);
                        InputStream is = httpExchange.getRequestBody();
                        Client client = parseRequestBody(is);
                        repository.update(key, client);
                        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        writeMessage(httpExchange.getResponseBody(), HTTP_MESSAGE_OK);
                    });
                    break;

                case "PUT":
                    runMethod(httpExchange, paths -> {
                        if (paths.length != 0)
                            throw new NumberFormatException();
                        InputStream is = httpExchange.getRequestBody();
                        Client client = parseRequestBody(is);
                        long id = repository.create(client);
                        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, 0);
                        writeMessage(httpExchange.getResponseBody(), HTTP_MESSAGE_OK, "ID", Long.toString(id));
                    });
                    break;

                case "DELETE":
                    runMethod(httpExchange, paths -> {
                        if (paths.length != 1)
                            throw new NumberFormatException();
                        repository.delete(Long.parseLong(paths[0]));
                        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
                        writeMessage(httpExchange.getResponseBody(), HTTP_MESSAGE_OK);
                    });
                    break;

                default:
                    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_IMPLEMENTED, 0);
                    writeMessage(httpExchange.getResponseBody(), "Not implemented yet");
            }
            httpExchange.getResponseBody().close();
        }
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }
}
