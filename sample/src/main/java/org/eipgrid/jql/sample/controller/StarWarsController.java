package org.eipgrid.jql.sample.controller;

import org.eipgrid.jql.jdbc.JdbcSchemaController;
import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JdbcSchemaController {

    public StarWarsController(JQLJdbcService service) {
        super(service, "starwars");
    }
}
