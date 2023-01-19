package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.JqlRequest;
import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.JqlRepository;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

public abstract class JdbcController {

    private final JdbcJqlService service;
    private final String default_namespace;

    public JdbcController(JdbcJqlService service, String default_namespace) {
        this.service = service;
        this.default_namespace = default_namespace;
    }

    @GetMapping(path = "/{table}/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    public KVEntity get(@PathVariable("table") String table,
                        @PathVariable("id") String id$) {
        JqlRepository<KVEntity, Object> repository = getRepository(table);
        Object id = repository.convertId(id$);
        KVEntity entity = repository.find(id);
        if (entity == null) {
            throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
        }
        return entity;
    }

    @PostMapping(path = "/{table}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 검색")
    public Object select_form(@PathVariable("table") String table,
                              @Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"query\": { } }")
                              @ModelAttribute JqlRequest.Builder request) {
        return select(table, request);
    }

    @PostMapping(path = "/{table}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 검색")
    public Object select(@PathVariable("table") String table,
                         @Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"query\": { } }")
                         @RequestBody JqlRequest.Builder request) {
        JqlRepository<KVEntity, Object> repository = getRepository(table);
        List<KVEntity> res = repository.select(request.build());
        return KVEntity.of("content", res);
    }

    @PutMapping(path = "/{table}/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    public KVEntity add(@PathVariable("table") String table,
                        @Schema(implementation = Object.class)
                        @RequestBody Map<String, Object> entity) throws Exception {
        JqlRepository<KVEntity, Object> repository = getRepository(table);
        Object id = repository.insert(entity);
        KVEntity newEntity = repository.find(id);
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
        JqlRepository<KVEntity, Object> repository = getRepository(table);
        repository.update(idList, updateSet);
        List<KVEntity> entities = repository.list(idList);
        return entities;
    }

    @DeleteMapping("/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    public Collection<String> delete(@PathVariable("table") String table,
                                     @Schema(implementation = String.class)
                                     @PathVariable("idList") Collection<String> idList) {
        JqlRepository<KVEntity, Object> repository = getRepository(table);
        repository.delete(idList);
        return idList;
    }

    protected JqlRepository<KVEntity, Object> getRepository(String tableName) {
        String tablePath = service.makeTablePath(default_namespace, tableName);
        return service.makeRepository(tablePath);
    }

}
