package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

public abstract class JdbcController {

    private final JQLJdbcService service;
    private final String default_namespace;

    public JdbcController(JQLJdbcService service, String default_namespace) {
        this.service = service;
        this.default_namespace = default_namespace;
    }

    @GetMapping(path = "/{table}/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    public KVEntity get(@PathVariable("table") String table,
                        @PathVariable("id") String id$) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        Object id = repository.convertId(id$);
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
                       @RequestParam(value = "limit", defaultValue = "0") int limit,
//                       @RequestParam(value = "columns", required = false) String columns,
                       @RequestParam(value = "sort", required = false) String sort) {
        return find(table, page, limit, sort,null);
    }

    @PostMapping(path = "/{table}/find")
    @ResponseBody
    @Operation(summary = "조건 검색")
    public Object find(@PathVariable("table") String table,
                       @RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", defaultValue = "0") int limit,
//                       @RequestParam(value = "columns", required = false) String columns,
                       @RequestParam(value = "sort", required = false) String sort,
                       @Schema(implementation = Object.class)
                       @RequestBody() HashMap<String, Object> filter) {
        boolean need_pagination = page != null && limit > 1;
        int offset = need_pagination ? page * limit : 0;
        JqlSelect select = JqlSelect.by(null, sort, offset, limit);

        JQLRepository<KVEntity, Object> repository = getRepository(table);
        List<KVEntity> res = repository.find(filter, select);

        if (need_pagination) {
            long count = repository.count(filter);
            PageRequest pageReq = PageRequest.of(page, limit, select.getSort());
            return new PageImpl(res, pageReq, count);
        } else {
            return res;
        }
    }

    @PostMapping(path = "/{table}/top")
    @ResponseBody
    @Operation(summary = "조건 검색 첫 엔터티 읽기")
    public KVEntity top(@PathVariable("table") String table,
//                        @RequestParam(value = "columns", required = false) String columns,
                        @RequestParam(value = "sort", required = false) String sort,
                        @Schema(implementation = Object.class)
                        @RequestBody HashMap<String, Object> filter) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        JqlSelect select = JqlSelect.by(null, sort, 0, 1);
        List<KVEntity>  res = repository.find(filter, select);
        return res.size() > 0 ? res.get(0) : null;
    }

    @PostMapping(path = "/{table}/", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Operation(summary = "엔터티 추가")
    public KVEntity add(@PathVariable("table") String table,
                        @Schema(implementation = Object.class)
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
                       @Schema(implementation = String.class)
                       @PathVariable("idList") Collection<Object> idList,
                       @Schema(implementation = Object.class)
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
                                     @Schema(implementation = String.class)
                                     @PathVariable("idList") Collection<String> idList) {
        JQLRepository<KVEntity, Object> repository = getRepository(table);
        repository.delete(idList);
        return idList;
    }

    JQLRepository<KVEntity, Object> getRepository(String tableName) {
        String tablePath = service.makeTablePath(default_namespace, tableName);
        return service.makeRepository(tablePath);
    }

}
