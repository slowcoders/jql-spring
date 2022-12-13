package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.jdbc.JdbcSchemaController;
import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JdbcSchemaController {

    public StarWarsController(JQLJdbcService service) {
        super(service, "starwars");
    }

}
