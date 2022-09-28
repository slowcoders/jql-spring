package org.slowcoders.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slowcoders.jql.JQLReadOnlyController;
import org.slowcoders.jql.JQLRepository;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.metadata.JdbcSchema;
import org.slowcoders.jql.json.JsonJql;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultJdbcController<ID> {

    private final JQLJdbcService service;

    public DefaultJdbcController(JQLJdbcService service) {
        this.service = service;
    }

    @GetMapping(path = "/{schema}/{table}/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    public KVEntity get(@PathVariable("schema") String schema, @PathVariable("table") String table, @PathVariable("id") ID id) {
        JQLRepository<KVEntity, ID> repository = getRepository(schema, table);
        KVEntity entity = repository.find(id);
        if (entity == null) {
            throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
        }
        return entity;
    }

    @GetMapping(path = "/{schema}/{table}/")
    @ResponseBody
    @Operation(summary = "전체 엔터티 리스트")
    public Object list(@PathVariable("schema") String schema, @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort) {
        return find(schema, table, page, limit, _sort, null);
    }

    @PostMapping(path = "/{schema}/{table}/find")
    @ResponseBody
    @Operation(summary = "조건 검색")
    public Object find(@PathVariable("schema") String schema, @PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        JQLRepository<KVEntity, ID> repository = getRepository(schema, table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        if (page == null) {
            return repository.find(filter, sort, limit == null ? -1 : limit);
        }

        page = page - 1;
        PageRequest pageReq = sort == null ?
                PageRequest.of(page, limit) : PageRequest.of(page, limit, sort);
        return repository.find(filter, pageReq);
    }

    @PostMapping(path = "/{schema}/{table}/top")
    @ResponseBody
    @Operation(summary = "조건 검색 첫 엔터티 읽기")
    public KVEntity top(@PathVariable("schema") String schema, @PathVariable("table") String table,
                        @RequestParam(value = "sort", required = false) String[] _sort,
                        @RequestBody HashMap<String, Object> filter) {
        JQLRepository<KVEntity, ID> repository = getRepository(schema, table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        return repository.findTop(filter, sort);
    }

    @PostMapping(path = "/{schema}/{table}/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    public KVEntity add(@PathVariable("schema") String schema, @PathVariable("table") String table,
                        @RequestBody Map<String, Object> entity) throws Exception {
        JQLRepository<KVEntity, ID> repository = getRepository(schema, table);
        ID id = repository.insert(entity);
        KVEntity newEntity = repository.find(id);
        return newEntity;
    }

    @PatchMapping(path = "/{schema}/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 일부 내용 변경")
    public List update(@PathVariable("schema") String schema, @PathVariable("table") String table,
                       @PathVariable("idList") Collection<ID> idList,
                       @RequestBody HashMap<String, Object> updateSet) throws Exception {
        JQLRepository<KVEntity, ID> repository = getRepository(schema, table);
        repository.update(idList, updateSet);
        List<KVEntity> entities = repository.list(idList);
        return entities;
    }

    @DeleteMapping("/{schema}/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    public Collection<ID> delete(@PathVariable("schema") String schema, @PathVariable("table") String table,
                                 @PathVariable("idList") Collection<ID> idList) {
        JQLRepository<KVEntity, ID> repository = getRepository(schema, table);
        repository.delete(idList);
        return idList;
    }

    JQLRepository<KVEntity, ID> getRepository(String dbSchema, String tableName) {
        String tablePath = service.makeTablePath(dbSchema, tableName);
        return service.makeRepository(tablePath);
    }

    private JqlSchema getSchema(String dbSchema, String tableName) throws Exception {
        String tablePath = service.makeTablePath(dbSchema, tableName);
        JQLRepository repo = service.makeRepository(tablePath);
        if (repo.getEntityType() != KVEntity.class) {
            return service.loadSchema(repo.getEntityType());
        }
        return service.loadSchema(tablePath);
    }

    private List<String> getAllTableNames(String dbSchema) throws Exception {
        return service.getTableNames(dbSchema);
    }

    @GetMapping("/metadata/jpa/{schema}/{table}")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성")
    public String jpaSchema(@PathVariable("schema") String dbSchema,
                            @PathVariable("table") String tableName) throws Exception {
        JqlSchema schema = getSchema(dbSchema, tableName);
        String source;
        if (schema instanceof JdbcSchema) {
            source = ((JdbcSchema)schema).dumpJPAEntitySchema();
        }
        else {
            source = dbSchema + '.' + tableName + " is not a JdbcSchema";
        }
        return source;
    }

    @GetMapping("/metadata/jpa/{schema}/")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성 (DBSchema 전체)")
    public String jpaSchemas(@PathVariable("schema") String dbSchema) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : this.getAllTableNames(dbSchema)) {
            sb.append("\n\n//-------------------------------------------------//\n\n");
            String source = this.jpaSchema(dbSchema, tableName);
            sb.append(source);
        }
        return sb.toString();
    }


    @GetMapping("/metadata/json/{schema}/{table}")
    @ResponseBody
    @Operation(summary = "JSON Schema 소스 생성")
    public String jsonSchema(@PathVariable("schema") String dbSchema,
                             @PathVariable("table") String tableName) throws Exception {
        JqlSchema schema = getSchema(dbSchema, tableName);
        String source = JsonJql.createDDL(schema);
        String join = JsonJql.createJoinJQL(schema);
        StringBuilder sb = new StringBuilder();
        sb.append(source).append("\n\n").append(join);
        return sb.toString();
    }

    @GetMapping("/metadata/json/{schema}/")
    @ResponseBody
    @Operation(summary = "JSON Schema 소스 생성 (DBSchema 전체)")
    public String jsonSchemas(@PathVariable("schema") String dbSchema) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : this.getAllTableNames(dbSchema)) {
            JqlSchema schema = getSchema(dbSchema, tableName);
            sb.append("\n\n//-------------------------------------------------//\n\n");
            String source = JsonJql.createDDL(schema);
            String join = JsonJql.createJoinJQL(schema);
            sb.append(source).append("\n\n").append(join);
            sb.append(source);
        }
        return sb.toString();
    }

    @GetMapping("/{schema}/")
    @ResponseBody
    @Operation(summary = "지정된 DBSchema 에 속한 Table list")
    public String listTables(@PathVariable("schema") String dbSchema) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : this.getAllTableNames(dbSchema)) {
            sb.append(tableName).append('\n');
        }
        return sb.toString();
    }

    @GetMapping("/")
    @ResponseBody
    @Operation(summary = "DBSchema 목록")
    public String listSchemas() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getDBSchemas()) {
            sb.append(tableName).append('\n');
        }
        return sb.toString();
    }

}
