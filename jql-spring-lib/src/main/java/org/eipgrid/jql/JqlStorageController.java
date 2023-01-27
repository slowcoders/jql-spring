package org.eipgrid.jql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

public abstract class JqlStorageController {

    private final JqlService service;
    private final String default_namespace;

    public JqlStorageController(JdbcJqlService service, String default_namespace) {
        this.service = service;
        this.default_namespace = default_namespace;
    }

    @GetMapping(path = "/{table}/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    public Object get(@PathVariable("table") String table,
                        @PathVariable("id") String id$) {
        JqlRepository repository = getRepository(table);
        Object id = repository.convertId(id$);
        Object entity = repository.find(id);
        if (entity == null) {
            throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
        }
        return entity;
    }

    @PostMapping(path = "/{table}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 검색")
    public Object find_form(@PathVariable("table") String table,
                              @Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                              @ModelAttribute JqlQuery.Request request) {
        return find(table, request);
    }

    @PostMapping(path = "/{table}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 검색")
    public Object find(@PathVariable("table") String table,
                         @Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                         @RequestBody JqlQuery.Request request) {
        JqlRepository repository = getRepository(table);
        return request.buildQuery(repository).execute();
    }

    @PutMapping(path = "/{table}/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    public Object add(@PathVariable("table") String table,
                        @Schema(implementation = Object.class)
                        @RequestBody Map<String, Object> entity) throws Exception {
        JqlRepository repository = getRepository(table);
        Object id = repository.insert(entity);
        Object newEntity = repository.find(id);
        return newEntity;
    }

    @PatchMapping(path = "/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 일부 내용 변경")
    public List update(@PathVariable("table") String table,
                       @Schema(implementation = String.class)
                       @PathVariable("idList") Collection<Object> idList,
                       @Schema(implementation = Object.class)
                       @RequestBody HashMap<String, Object> updateSet) throws Exception {
        JqlRepository repository = getRepository(table);
        repository.update(idList, updateSet);
        List<?> entities = repository.find(idList);
        return entities;
    }

    @DeleteMapping("/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    public Collection<String> delete(@PathVariable("table") String table,
                                     @Schema(implementation = String.class)
                                     @PathVariable("idList") Collection<String> idList) {
        JqlRepository repository = getRepository(table);
        repository.delete(idList);
        return idList;
    }

    protected JqlRepository getRepository(String tableName) {
        String tablePath = service.makeTablePath(default_namespace, tableName);
        return service.getRepository(tablePath);
    }

}
