package org.example.service.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.dao.CrudRepository;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.JsonException;
import org.example.service.server.method.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.example.service.JsonConverter.writeMessage;
import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_INVALID_REQUEST;
import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_NOT_FOUND;

public class HttpRestService implements ServerService {

    private static final String CONTEXT = "/v1/user";

    private CrudRepository<Long, User> repository;

    private HttpServer server;

    public HttpRestService(CrudRepository<Long, User> repository, String host, int port) throws IOException {
        this.repository = repository;
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
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

        void runMethod(HttpExchange httpExchange, HttpMethodRunner runner) throws IOException {
            String[] paths = getPaths(httpExchange.getRequestURI().getPath());
            try {
                Response response = runner.run(paths, httpExchange.getRequestBody(), repository);
                httpExchange.sendResponseHeaders(response.getCode(), 0);
                httpExchange.getResponseBody().write(response.getBody().toString().getBytes());
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
                    runMethod(httpExchange, new Get());
                    break;

                case "POST":
                    runMethod(httpExchange, new Post());
                    break;

                case "PUT":
                    runMethod(httpExchange, new Put());
                    break;

                case "DELETE":
                    runMethod(httpExchange, new Delete());
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
