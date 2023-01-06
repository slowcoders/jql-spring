package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.jdbc.JdbcController;
import org.eipgrid.jql.jdbc.JdbcJQService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JdbcController {

    public StarWarsController(JdbcJQService service) {
        super(service, "starwars");
    }

    @GetMapping(path = "/{table}/last-executed-sql")
    @ResponseBody
    @Operation(summary = "마지막 실행 SQL 문 보기")
    public String last_executed_sql(@PathVariable String table) {
        return ((JDBCRepositoryBase)super.getRepository(table)).getLastExecutedSql();
    }
}
