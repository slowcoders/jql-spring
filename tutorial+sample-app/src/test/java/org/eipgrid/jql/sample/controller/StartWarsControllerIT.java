package org.eipgrid.jql.sample.controller;

import org.eipgrid.jql.sample.model.Character;
import org.eipgrid.jql.sample.test.TestWebClient;
import org.eipgrid.jql.sample.test.TestEnv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class StartWarsControllerIT {

    @Autowired
    private TestWebClient webClient;

    @BeforeAll
    public static void beforeAll() {
        TestEnv.startPostgres();
    }

    @Test
    public void should_return_list_of_characters() {
        ResponseEntity<List<Character>> response = webClient.list();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertTrue(response.getBody().size() > 0);
    }

    @Test
    public void should_return_character_with_id_in_jql() {
        String jql =
        """
        { "id": 1001 }
        """;

        ResponseEntity<List<Character>> response = this.webClient.find(jql);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Character> result = response.getBody();

        assertEquals(1, result.size());
        assertEquals(1001, result.get(0).getId());
    }
}
