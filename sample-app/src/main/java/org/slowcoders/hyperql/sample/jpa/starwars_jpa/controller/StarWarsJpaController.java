package org.slowcoders.hyperql.sample.jpa.starwars_jpa.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.hyperql.HyperStorageController;
import org.slowcoders.hyperql.OutputFormat;
import org.slowcoders.hyperql.sample.jpa.starwars_jpa.service.StarWarsJpaService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/hql/starwars_jpa")
public class StarWarsJpaController extends HyperStorageController.CRUD implements HyperStorageController.ListAll {

    private final StarWarsJpaService service;

    public StarWarsJpaController(StarWarsJpaService service, ConversionService conversionService) {
        super(service.getStorage(), "starwars_jpa", conversionService);
        this.service = service;
    }

    @Override
    public Response find(
            @PathVariable("table") String table,
            @RequestParam(value = "output", required = false) OutputFormat output,
            @RequestParam(value = "select", required = false) String select,
            @Schema(implementation = String.class)
            @RequestParam(value = "sort", required = false) String[] orders,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "distinct", required = false) Boolean distinct,
            @Schema(implementation = Object.class)
            @RequestBody Map<String, Object> filter) {
        Response resp = super.find(table, output, select, orders, page, limit, distinct, filter);
        resp.setProperty("lastExecutedSql", resp.getQuery().getExecutedQuery());
        return resp;
    }

    @GetMapping("/loadData")
    public void loadData() throws IOException {
        service.loadData();
    }
}
