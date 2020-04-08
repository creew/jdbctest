package org.example.exceptions;

public class CrudExceptionNotFound extends CrudException {
    public CrudExceptionNotFound() {
        super("Not found");
    }
}
