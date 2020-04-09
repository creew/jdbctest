package org.example;

import org.example.entity.Client;
import org.example.exceptions.CrudException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) throws CrudException {
        ApplicationContext fileSystemXmlApplicationContext = new ClassPathXmlApplicationContext("springContext.xml");
        CrudRepository<Long, Client> repository =  fileSystemXmlApplicationContext.getBean(CrudRepository.class);
        Long key = repository.create(new Client("Tutanhomon", "I"));
        Client client = repository.read(key);
        System.out.println(client);
    }
}
