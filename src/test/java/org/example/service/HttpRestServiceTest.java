package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.entity.Client;
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
@ContextConfiguration("/testSpringContext.xml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpRestServiceTest {

    private static final String CONTEXT = "/v1/client";

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
    RestService service;

    @Test
    void shouldPutBeOk() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertDoesNotThrow(() -> responseEntity.getBody().get("ID").asLong());
    }

    @Test
    void shouldPutWithWrongPathFail() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.exchange(PATH + "/he", HttpMethod.PUT, request, JsonNode.class));
    }

    @Test
    void shouldPutGetBeOk() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        ResponseEntity<Client> clientResponseEntity = restTemplate.getForEntity(PATH + "/" + id, Client.class);
        assertEquals(clientResponseEntity.getBody().getFirstName(), "bar");
        assertEquals(clientResponseEntity.getBody().getLastName(), "sadas");
    }

    @Test
    void shouldPutGetWrongIdFail() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(PATH + "/" + (id + 1000), Client.class));
    }

    @Test
    void shouldPutGetWrongPathFail() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(PATH + "/path/" + id, Client.class));
    }

    @Test
    void shouldPutDeleteGetBeFail() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(PATH, HttpMethod.PUT, request, JsonNode.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        long id = responseEntity.getBody().get("ID").asLong();
        ResponseEntity<JsonNode> responseDelete = restTemplate.exchange(PATH + "/" + id
                , HttpMethod.DELETE
                ,null, JsonNode.class);
        assertEquals(responseDelete.getStatusCode(), HttpStatus.NO_CONTENT);
        assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(PATH + "/" + id, Client.class));
    }

    @Test
    void shouldPutDeleteWrongPathBeFail() {
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
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
        HttpEntity<Client> request = new HttpEntity<>(new Client("bar", "sadas"));
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
        Client first = new Client("bar", "sadas");
        ResponseEntity<JsonNode> responsePut = restTemplate.exchange(PATH,
                HttpMethod.PUT, new HttpEntity<>(first), JsonNode.class);
        assertEquals(HttpStatus.CREATED, responsePut.getStatusCode());
        long id = responsePut.getBody().get("ID").asLong();
        ResponseEntity<Client> responseGet = restTemplate.getForEntity(PATH + "/" + id, Client.class);
        assertEquals(HttpStatus.OK, responseGet.getStatusCode());
        assertEquals(first, responseGet.getBody());
        first.setFirstName("beer");
        first.setLastName("bur");
        ResponseEntity<JsonNode> responsePost = restTemplate.exchange(PATH + "/" + id,
                HttpMethod.POST, new HttpEntity<>(first), JsonNode.class);
        assertEquals(HttpStatus.OK, responsePost.getStatusCode());
        ResponseEntity<Client> responseGet2 = restTemplate.getForEntity(PATH + "/" + id, Client.class);
        assertEquals(HttpStatus.OK, responseGet2.getStatusCode());
        assertEquals(first, responseGet2.getBody());
    }
}