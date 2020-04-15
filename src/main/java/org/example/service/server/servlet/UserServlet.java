package org.example.service.server.servlet;

import org.eclipse.jetty.io.WriterOutputStream;
import org.example.dao.CrudRepository;
import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.JsonException;
import org.example.service.JsonConverter;
import org.example.service.server.method.*;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_INVALID_REQUEST;
import static org.example.service.server.method.CodesConstants.HTTP_MESSAGE_NOT_FOUND;

public class UserServlet extends HttpServlet {

    private CrudRepository<Long, User> repo;

    @Override
    public void init(ServletConfig config) {
        ApplicationContext ac = (ApplicationContext)config.getServletContext().getAttribute("applicationContext");
        this.repo = (CrudRepository<Long, User>)ac.getBean("crudRepository");
    }

    private String[] parsePath(String pathInfo) {
        String[] ret = {};
        if (pathInfo != null && pathInfo.length() > 1) {
            pathInfo = pathInfo.substring(1);
            ret = pathInfo.split("/");
        }
        return ret;
    }

    private void runMethod(HttpServletRequest req, HttpServletResponse resp, HttpMethodRunner runner) throws IOException {
        String[] paths = parsePath(req.getPathInfo());
        resp.setContentType("application/json");
        Writer out = resp.getWriter();
        try ( Response response = runner.run(paths, req.getInputStream(), repo) ){
            resp.setStatus(response.getCode());
            out.write(response.getBody().toString());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonConverter.writeMessage(new WriterOutputStream(out), HTTP_MESSAGE_INVALID_REQUEST);
        } catch (CrudException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonConverter.writeMessage(new WriterOutputStream(out), HTTP_MESSAGE_NOT_FOUND);
        } catch (JsonException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonConverter.writeMessage(new WriterOutputStream(out), e.getMessage());
        }
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        runMethod(req, resp, new Get());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        runMethod(req, resp, new Post());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        runMethod(req, resp, new Put());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        runMethod(req, resp, new Delete());
    }
}
