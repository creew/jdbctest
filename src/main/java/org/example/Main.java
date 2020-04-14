package org.example;

import org.example.service.server.ServerService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext fileSystemXmlApplicationContext = new ClassPathXmlApplicationContext("springContext.xml");
        ServerService serverService = fileSystemXmlApplicationContext.getBean(ServerService.class);
        serverService.start();
    }
}
