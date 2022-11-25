package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.jdbc.metadata.JdbcSchema;
import org.eipgrid.jql.spring.JQLReadOnlyController;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

public abstract class JdbcSchemaController {

    private final JQLJdbcService service;
    private final String db_schema;

    public JdbcSchemaController(JQLJdbcService service, String db_schema) {
        this.service = service;
        this.db_schema = db_schema;
    }

    @GetMapping("/")
    @ResponseBody
    @Operation(summary = "전체 Table 목록")
    public String listTables() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getTableNames(db_schema)) {
            sb.append(tableName).append('\n');
        }
        return sb.toString();
    }

    @GetMapping(path = "/{table}/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    public KVEntity get(@PathVariable("table") String table, @PathVariable("id") Object id) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        KVEntity entity = repository.find(id);
        if (entity == null) {
            throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
        }
        return entity;
    }

    @GetMapping(path = "/{table}/")
    @ResponseBody
    @Operation(summary = "전체 엔터티 리스트")
    public Object list(@PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort) {
        return find(table, page, limit, _sort, null);
    }

    @PostMapping(path = "/{table}/find")
    @ResponseBody
    @Operation(summary = "조건 검색")
    public Object find(@PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        if (page == null) {
            return repository.find(filter, sort, limit == null ? -1 : limit);
        }

        PageRequest pageReq = sort == null ?
                PageRequest.of(page, limit) : PageRequest.of(page, limit, sort);
        return repository.find(filter, pageReq);
    }

    @PostMapping(path = "/{table}/top")
    @ResponseBody
    @Operation(summary = "조건 검색 첫 엔터티 읽기")
    public KVEntity top(@PathVariable("table") String table,
                        @RequestParam(value = "sort", required = false) String[] _sort,
                        @RequestBody HashMap<String, Object> filter) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        Sort sort = JQLReadOnlyController.buildSort(_sort);
        return repository.findTop(filter, sort);
    }

    @GetMapping("/{table}/metadata/columns")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성")
    public List<String> columns(@PathVariable("table") String tableName) throws Exception {
        String tablePath = service.makeTablePath(db_schema, tableName);
        JqlSchema schema = service.loadSchema(tablePath);
        ArrayList<String> columns = new ArrayList<>();
        for (JqlColumn column : schema.getReadableColumns()) {
            columns.add(column.getJsonKey());
        }
        return columns;
    }

    @PostMapping(path = "/{table}/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    public KVEntity add(@PathVariable("table") String table,
                        @RequestBody Map<String, Object> entity) throws Exception {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        Object id = repository.insert(entity);
        KVEntity newEntity = repository.find(id);
        return newEntity;
    }

    @PatchMapping(path = "/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 일부 내용 변경")
    public List update(@PathVariable("table") String table,
                       @PathVariable("idList") Collection<Object> idList,
                       @RequestBody HashMap<String, Object> updateSet) throws Exception {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        repository.update(idList, updateSet);
        List<KVEntity> entities = repository.list(idList);
        return entities;
    }

    @DeleteMapping("/{table}/{idList}")
    @ResponseBody
    @Operation(summary = "엔터티 삭제")
    public Collection<String> delete(@PathVariable("table") String table,
                                 @PathVariable("idList") Collection<String> idList) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        repository.delete(idList);
        return idList;
    }

    JQLRepository<KVEntity, Object> getRepository(String tableName) {
        String tablePath = service.makeTablePath(db_schema, tableName);
        return service.makeRepository(tablePath);
    }

    private JqlSchema getSchema(String tableName) throws Exception {
        String tablePath = service.makeTablePath(db_schema, tableName);
        JQLRepository repo = service.makeRepository(tablePath);
        if (repo.getEntityType() != KVEntity.class) {
            return service.loadSchema(repo.getEntityType());
        }
        return service.loadSchema(tablePath);
    }

    @GetMapping("/metadata/jpa/{schema}/{table}")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성")
    public String jpaSchema(@PathVariable("table") String tableName) throws Exception {
        JqlSchema schema = getSchema(tableName);
        String source;
        if (schema instanceof JdbcSchema) {
            source = ((JdbcSchema)schema).dumpJPAEntitySchema();
        }
        else {
            source = tableName + " is not a JdbcSchema";
        }
        return source;
    }

}
