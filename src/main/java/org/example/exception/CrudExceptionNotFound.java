package org.example.exception;

public class CrudExceptionNotFound extends CrudException {
    public CrudExceptionNotFound() {
        super("Not found");
    }
}
