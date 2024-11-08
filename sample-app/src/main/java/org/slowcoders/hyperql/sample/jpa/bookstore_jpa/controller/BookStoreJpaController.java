package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.hyperql.HyperStorageController;
import org.slowcoders.hyperql.OutputOptions;
import org.slowcoders.hyperql.sample.jpa.bookstore_jpa.service.BookStoreJpaService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/hql/bookstore_jpa")
public class BookStoreJpaController extends HyperStorageController.CRUD implements HyperStorageController.ListAll {

    private final BookStoreJpaService service;

    public BookStoreJpaController(BookStoreJpaService service, ConversionService conversionService) {
        super(service.getStorage(), "bookstore_jpa", conversionService);
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
//        service.loadData();
    }
}
