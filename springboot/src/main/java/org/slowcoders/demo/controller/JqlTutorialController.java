package org.slowcoders.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slowcoders.jql.JQLReadOnlyController;
import org.slowcoders.jql.JQLRepository;
import org.slowcoders.jql.jdbc.DefaultJdbcController;
import org.slowcoders.jql.jdbc.JQLJdbcService;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;

@RestController
@RequestMapping("/api/jql-tutorial")
public class JqlTutorialController {

    private final JQLJdbcService service;

    public JqlTutorialController(JQLJdbcService service) {
        this.service = service;
    }

    JQLRepository<KVEntity, Object> getRepository(String dbSchema, String tableName) {
        String tablePath = service.makeTablePath(dbSchema, tableName);
        return service.makeRepository(tablePath);
    }

    @PostMapping(path = "/{schema}/{table}/pagination")
    @ResponseBody
    @Operation(summary = "Pagination 처리",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientRequest body.",
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "limit 과 page 값을 변경하여 Pagination 검색을 수행한다.")
    public Object pagination(@Parameter(example = "public") @PathVariable("schema") String schema,
                       @Parameter(example = "owners") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "5")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        JQLRepository<KVEntity, Object> repository = getRepository(schema, table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        if (page == null) {
            return repository.find(filter, sort, limit == null ? -1 : limit);
        }

        page = page - 1;
        PageRequest pageReq = sort == null ?
                PageRequest.of(page, limit) : PageRequest.of(page, limit, sort);
        return repository.find(filter, pageReq);
    }

    @PostMapping(path = "/{schema}/{table}/sort")
    @ResponseBody
    @Operation(summary = "Sort 처리",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientRequest body.",
                    content = @Content(schema = @Schema(implementation = Object.class)), required = true),
            description = "Sort 값을 추가하여 정렬된 결과를 얻는다.")
    public Object sort(@Parameter(example = "public") @PathVariable("schema") String schema,
                       @Parameter(example = "owners") @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "5")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @Parameter(description = "'firstName, -lastName'을 입력하여 정렬 결과를 확인한다.", schema = @Schema(implementation = String.class))
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        JQLRepository<KVEntity, Object> repository = getRepository(schema, table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        if (page == null) {
            return repository.find(filter, sort, limit == null ? -1 : limit);
        }

        page = page - 1;
        PageRequest pageReq = sort == null ?
                PageRequest.of(page, limit) : PageRequest.of(page, limit, sort);
        return repository.find(filter, pageReq);
    }
}
