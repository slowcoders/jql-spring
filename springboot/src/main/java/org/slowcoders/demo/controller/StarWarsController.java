package org.slowcoders.demo.controller;

import org.slowcoders.jql.jdbc.JdbcSchemaController;
import org.slowcoders.jql.jdbc.JQLJdbcService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JdbcSchemaController {

    public StarWarsController(JQLJdbcService service) {
        super(service, "starwars");
    }
}
