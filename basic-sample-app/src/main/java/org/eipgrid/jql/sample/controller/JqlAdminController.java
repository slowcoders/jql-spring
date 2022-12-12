package org.eipgrid.jql.sample.controller;

import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.eipgrid.jql.jdbc.JdbcDatabaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/admin")
public class JqlAdminController extends JdbcDatabaseController {

    public JqlAdminController(JQLJdbcService service) {
        super(service);
    }
}
