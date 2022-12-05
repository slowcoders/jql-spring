package org.eipgrid.jql.spring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.eipgrid.jql.JqlSelect;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class JQLReadOnlyController<ENTITY, ID> {

    private JQLRepository<ENTITY,ID> repository;

    protected JQLReadOnlyController(JQLRepository<ENTITY, ID> repository) {
        this.repository = repository;
    }

    protected JQLRepository<ENTITY,ID> getRepository() {
        return repository;
    }

    protected void setRepository(JQLRepository<ENTITY, ID> repository) {
        if (this.repository != null) {
            throw new RuntimeException("repository already assigned");
        }
        this.repository = repository;
    }

    public static HashMap newSimpleMap(String key, Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @GetMapping(path = "/{id}")
    @ResponseBody
    @Operation(summary = "지정 엔터티 읽기")
    public ENTITY get(@PathVariable("id") ID id) {
        ENTITY entity = repository.find(id);
        if (entity == null) {
            throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
        }
        return entity;
    }

    @GetMapping(path = "/")
    @ResponseBody
    @Operation(summary = "전체 엔터티 리스트")
    public Object list(@RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort) {
        return find(page, limit, _sort, null);
    }

    @PostMapping(path = "/find")
    @ResponseBody
    @Operation(summary = "조건 검색")
    public Object find(@RequestParam(value = "page", required = false) Integer page,
                       @Parameter(name = "limit", example = "10")
                       @RequestParam(value = "limit", required = false) Integer _limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        int limit = _limit == null ? 0 : _limit;
        boolean need_pagination = page != null && limit > 1;
        int offset = need_pagination ? page * limit : 0;
        Sort sort = JqlSelect.buildSort(_sort);
        List<ENTITY> res = repository.find(filter, JqlSelect.by(sort, offset, limit));

        if (need_pagination) {
            long count = repository.count(filter);
            PageRequest pageReq = PageRequest.of(page, limit, sort);
            return new PageImpl(res, pageReq, count);
        } else {
            return res;
        }
    }

    @PostMapping(path = "/top")
    @ResponseBody
    @Operation(summary = "조건 검색 첫 엔터티 읽기")
    public ENTITY top(@RequestParam(value = "sort", required = false) String[] _sort,
                      @RequestBody HashMap<String, Object> filter) {
        Sort sort = JqlSelect.buildSort(_sort);
        return repository.findTop(filter, sort);
    }

    @PostMapping(path = "/clear-cache")
    @ResponseBody
    @Operation(summary = "Cache 비우기")
    public void clearCache() {
        repository.clearEntityCache(null);
    }


}
