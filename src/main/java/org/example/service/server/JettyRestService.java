package org.example.service.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.InetSocketAddress;

public class JettyRestService implements ServerService {

    private final Server serverInstance;

    public JettyRestService(String host, int port) {
        serverInstance = new Server(new InetSocketAddress(host, port));

/*
        String contextPath = "/v1";
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.addEventListener(new SpringApplicationContextListener());
        servletContextHandler.addServlet(ClientServlet.class, ClientServlet.SERVLET_PATH);
        serverInstance.setHandler(servletContextHandler);
*/

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setServer(serverInstance);
        webAppContext.setDescriptor("src/main/webapp" + "/WEB-INF/web.xml");
        webAppContext.setResourceBase("src/main/webapp");
        webAppContext.setContextPath("/v1");
        serverInstance.setHandler(webAppContext);

    }

    @Override
    public void start() {
        try {
            if (!serverInstance.isStarted()) {
                serverInstance.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            if (!serverInstance.isStopped()) {
                serverInstance.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
