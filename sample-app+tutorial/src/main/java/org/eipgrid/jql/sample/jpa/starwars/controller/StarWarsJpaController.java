package org.eipgrid.jql.sample.jpa.starwars.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.JqlRepository;
import org.eipgrid.jql.JqlStorageController;
import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/starwars_jpa")
public class StarWarsJpaController extends JqlStorageController.CRUD implements JqlStorageController.ListAll {

    public StarWarsJpaController(JdbcJqlService service) {
        super(service, "starwars_jpa.");
    }

    @Override
    public JqlQuery.Response find(@PathVariable("table") String table,
                                  @RequestBody JqlQuery.Request request) {
        JqlRepository repository = getRepository(table);
        JqlQuery query = request.buildQuery(repository);
        JqlQuery.Response resp = query.execute();
        resp.setProperty("lastExecutedSql", query.getExecutedQuery());
        return resp;
    }
}
