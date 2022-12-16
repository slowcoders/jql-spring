package org.eipgrid.jql.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.jdbc.JdbcController;
import org.eipgrid.jql.jdbc.JQLJdbcService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JdbcController {

    public StarWarsController(JQLJdbcService service) {
        super(service, "starwars");
    }

    @GetMapping(path = "/{table}/last-executed-sql")
    @ResponseBody
    @Operation(summary = "마지막 실행 SQL 문 보기")
    public String last_executed_sql(@PathVariable String table) {
        return ((JDBCRepositoryBase)super.getRepository(table)).getLastExecutedSql();
    }
}
