package org.example;

import org.example.service.RestService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext fileSystemXmlApplicationContext = new ClassPathXmlApplicationContext("springContext.xml");
        RestService restService = fileSystemXmlApplicationContext.getBean(RestService.class);
        restService.start();
    }
}
