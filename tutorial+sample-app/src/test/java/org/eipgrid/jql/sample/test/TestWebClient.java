package org.eipgrid.jql.sample.test;

import org.eipgrid.jql.sample.model.Character;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestWebClient {

    private static final String API_PATH = "/api/jql/starwars/character";

    @Autowired
    private TestRestTemplate restTemplate;

    public ResponseEntity<List<Character>> list() {
        return restTemplate.exchange(API_PATH + "/?select=*", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<List<Character>> find(String jql) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jql, headers);

        return restTemplate.exchange(API_PATH + "/find?select=*", HttpMethod.POST, request,
                new ParameterizedTypeReference<>() {
                });
    }
}
