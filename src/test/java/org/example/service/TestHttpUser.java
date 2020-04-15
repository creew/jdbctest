package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.entity.User;
import org.example.service.server.ServerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("/testSpringContextHttp.xml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHttpUser {

    private static final String CONTEXT = "/v1/user";

    private static final String PATH = "http://localhost:8080" + CONTEXT;

    private RestTemplate restTemplate;

    @BeforeAll
    void beforeAll() {
        restTemplate = new RestTemplate();
        service.start();
    }

    @AfterAll
    void afterAll() {
        service.stop();
    }

    @Autowired
    ServerService service;

    @Test
    void shouldPutBeOk() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertDoesNotThrow(() -> responseEntity.getBody().get("ID").asLong());
    }

    @Test
    void shouldPutWithWrongPathFail() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.exchange(PATH + "/he", HttpMethod.PUT, request, JsonNode.class));
    }

    @Test
    void shouldPutGetBeOk() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        ResponseEntity<User> clientResponseEntity = restTemplate.getForEntity(PATH + "/" + id, User.class);
        assertEquals("bar", clientResponseEntity.getBody().getLogin());
        assertEquals("sadas", clientResponseEntity.getBody().getPassword());
    }

    @Test
    void shouldPutGetWrongIdFail() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(PATH + "/" + (id + 1000), User.class));
    }

    @Test
    void shouldPutGetWrongPathFail() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(PATH + "/path/" + id, User.class));
    }

    @Test
    void shouldPutDeleteGetBeFail() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        ResponseEntity<JsonNode> responseDelete = restTemplate.exchange(PATH + "/" + id
                , HttpMethod.DELETE
                ,null, JsonNode.class);
        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(PATH + "/" + id, User.class));
    }

    @Test
    void shouldPutDeleteWrongPathBeFail() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.exchange(PATH + "/path/" + id
                , HttpMethod.DELETE
                ,null, JsonNode.class));
    }

    @Test
    void shouldPutDeleteWrongIdBeFail() {
        HttpEntity<User> request = new HttpEntity<>(new User("bar", "sadas", "123@123.ru"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.exchange(PATH + "/" + (id + 1000)
                        , HttpMethod.DELETE
                        ,null, JsonNode.class));
    }

    @Test
    void shouldPutUpdateGetOk() {
        User first = new User("bar", "sadas", "123@123.ru");
        ResponseEntity<JsonNode> responsePut = restTemplate.exchange(PATH,
                HttpMethod.PUT, new HttpEntity<>(first), JsonNode.class);
        assertEquals(HttpStatus.CREATED, responsePut.getStatusCode());
        long id = responsePut.getBody().get("ID").asLong();
        ResponseEntity<User> responseGet = restTemplate.getForEntity(PATH + "/" + id, User.class);
        assertEquals(HttpStatus.OK, responseGet.getStatusCode());
        assertEquals(first, responseGet.getBody());
        first.setLogin("beer");
        first.setPassword("bur");
        ResponseEntity<JsonNode> responsePost = restTemplate.exchange(PATH + "/" + id,
                HttpMethod.POST, new HttpEntity<>(first), JsonNode.class);
        assertEquals(HttpStatus.OK, responsePost.getStatusCode());
        ResponseEntity<User> responseGet2 = restTemplate.getForEntity(PATH + "/" + id, User.class);
        assertEquals(HttpStatus.OK, responseGet2.getStatusCode());
        assertEquals(first, responseGet2.getBody());
    }
}