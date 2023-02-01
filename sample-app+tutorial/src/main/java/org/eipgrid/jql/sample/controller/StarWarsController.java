package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.JqlStorageController;
import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JqlStorageController.CRUD implements JqlStorageController.ListAll {

    public StarWarsController(JdbcJqlService service) {
        super(service, "starwars");
    }

}
