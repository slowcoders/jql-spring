package org.eipgrid.jql.spring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.JqlSelect;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.List;

public interface JQLReadOnlyController<ENTITY, ID> {

    JQLRepository<ENTITY,ID> getRepository();

    static HashMap newSimpleMap(String key, Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @GetMapping(path = "/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    default ENTITY get(@PathVariable("id") ID id) {
        ENTITY entity = getRepository().find(id);
        if (entity == null) {
            throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
        }
        return entity;
    }

    @GetMapping(path = "/")
    @ResponseBody
    @Operation(summary = "전체 엔터티 리스트")
    default Object list(@RequestParam(value = "page", required = false) Integer page,
                        @Parameter(name = "limit", example = "10")
                        @RequestParam(value = "limit", defaultValue = "0") int limit,
                        @RequestParam(value = "columns", required = false) String columns,
                        @RequestParam(value = "sort", required = false) String sort) {
        return find(page, limit, columns, sort, null);
    }

    @PostMapping(path = "/find")
    @ResponseBody
    @Operation(summary = "조건 검색")
    default Object find(@RequestParam(value = "page", required = false) Integer page,
                        @Parameter(name = "limit", example = "10")
                        @RequestParam(value = "limit", defaultValue = "0") int limit,
                        @RequestParam(value = "columns", required = false) String columns,
                        @RequestParam(value = "sort", required = false) String sort,
                        @Schema(implementation = Object.class)
                        @RequestBody() HashMap<String, Object> filter) {
        boolean need_pagination = page != null && limit > 1;
        int offset = need_pagination ? page * limit : 0;
        JqlSelect select = JqlSelect.by(columns, sort, offset, limit);
        List<ENTITY> res = getRepository().find(filter, select);

        if (need_pagination) {
            long count = getRepository().count(filter);
            PageRequest pageReq = PageRequest.of(page, limit, select.getSort());
            return new PageImpl(res, pageReq, count);
        } else {
            return res;
        }
    }

    @PostMapping(path = "/top")
    @ResponseBody
    @Operation(summary = "조건 검색 첫 엔터티 읽기")
    default ENTITY top(@RequestParam(value = "columns", required = false) String columns,
                       @RequestParam(value = "sort", required = false) String sort,
                       @Schema(implementation = Object.class)
                       @RequestBody HashMap<String, Object> filter) {
        JqlSelect select = JqlSelect.by(columns, sort, 0, 1);
        List<ENTITY> res = getRepository().find(filter, select);
        return res.size() > 0 ? res.get(0) : null;
    }

    @PostMapping(path = "/clear-cache")
    @ResponseBody
    @Operation(summary = "Cache 비우기")
    default void clearCache() {
        getRepository().clearEntityCache(null);
    }


}
