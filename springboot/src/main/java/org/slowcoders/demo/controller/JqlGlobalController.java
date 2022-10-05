package org.slowcoders.demo.controller;

import org.slowcoders.jql.jdbc.DefaultJdbcController;
import org.slowcoders.jql.jdbc.JQLJdbcService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql")
public class JqlGlobalController extends DefaultJdbcController {

    public JqlGlobalController(JQLJdbcService service) {
        super(service);
    }
}
