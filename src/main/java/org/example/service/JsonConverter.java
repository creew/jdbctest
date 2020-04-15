package org.example.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.JsonException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JsonConverter {

    private JsonConverter() {
    }

    public static void writeMessage(OutputStream os, String message, String... keyval) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        map.put("Message", message);
        if (keyval.length > 0 && keyval.length % 2 == 0) {
            for (int i = 0; i < keyval.length; i += 2) {
                map.put(keyval[i], keyval[i + 1]);
            }
        }
        mapper.writeValue(os, map);
    }

    public static <T>T parseRequestBody(Class<T> clazz, InputStream is) throws JsonException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            return mapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new JsonException("Error parse body");
        }
    }

    public static Writer writeClient(Object object) throws JsonException {
        StringWriter sw = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(sw, object);
            return sw;
        } catch (IOException e) {
            throw new JsonException("Write class error");
        }
    }

}
