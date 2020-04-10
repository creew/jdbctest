package org.example.service;

import org.example.exceptions.CrudException;
import org.example.exceptions.JsonException;

import java.io.IOException;

@FunctionalInterface
public interface MethodRunner {
    void run(String[] paths) throws JsonException, CrudException, IOException;
}
