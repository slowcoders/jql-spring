package org.slowcoders.hyperql.sample.jpa.starwars_jpa.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.hyperql.HyperStorageController;
import org.slowcoders.hyperql.OutputOptions;
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
            OutputOptions req,
            @Schema(implementation = Object.class)
            @RequestBody Map<String, Object> filter) throws Exception {
        Response resp = super.find(table, req, filter);
        resp.setProperty("lastExecutedSql", resp.getQuery().getExecutedQuery());
        return resp;
    }

    @GetMapping("/loadData")
    public void loadData() throws IOException {
        service.loadData();
    }
}
