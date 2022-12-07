package org.eipgrid.jql.sample.controller;

import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.eipgrid.jql.jdbc.JdbcSchemaController;
import org.eipgrid.jql.jdbc.JdbcTableController;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/starwars/character-table")
public class CharacterController implements JdbcTableController<Object> {

    JQLRepository<KVEntity, Object> repository;

    public CharacterController(JQLJdbcService service) {
        this.repository = service.makeRepository("starwars.character");
    }

    @Override
    public JQLRepository getRepository() {
        return repository;
    }
}
