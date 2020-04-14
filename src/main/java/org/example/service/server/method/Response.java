package org.example.service.server.method;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Response implements Closeable {

    private int code;

    private Writer body;

    public Response(int code) {
        this.code = code;
        body = new StringWriter();
    }

    public Response(int code, Writer body) throws IOException {
        this(code);
        this.body.write(body.toString());
    }

    public int getCode() {
        return code;
    }

    public Writer getBody() {
        return body;
    }

    @Override
    public void close() throws IOException {
        body.close();
    }
}
