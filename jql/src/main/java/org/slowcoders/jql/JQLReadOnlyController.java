package org.slowcoders.jql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.HashMap;

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
                       @RequestParam(value = "limit", required = false) Integer limit,
                       @RequestParam(value = "sort", required = false) String[] _sort,
                       @RequestBody() HashMap<String, Object> filter) {
        Sort sort = buildSort(_sort);
        if (page == null) {
            return repository.find(filter, sort, limit == null ? -1 : limit);
        }

        page = page - 1;
        PageRequest pageReq = sort == null ?
                PageRequest.of(page, limit) : PageRequest.of(page, limit, sort);
        return repository.find(filter, pageReq);
    }

    @PostMapping(path = "/top")
    @ResponseBody
    @Operation(summary = "조건 검색 첫 엔터티 읽기")
    public ENTITY top(@RequestParam(value = "sort", required = false) String[] _sort,
                      @RequestBody HashMap<String, Object> filter) {
        Sort sort = buildSort(_sort);
        return repository.findTop(filter, sort);
    }

    @PostMapping(path = "/clear-cache")
    @ResponseBody
    @Operation(summary = "Cache 비우기")
    public void clearCache() {
        repository.clearEntityCache(null);
    }


    public static Sort buildSort(String columns[]) {
        if (columns == null || columns.length == 0) return null;

        ArrayList<Sort.Order> orders = new ArrayList<>();
        for (String col : columns) {
            col = col.trim();

            Sort.Order order;
            switch (col.charAt(0)) {
                case '-':
                    col = col.substring(1).trim();
                    order = Sort.Order.desc(col);
                    break;
                case '+':
                    col = col.substring(1).trim();
                    // no-break;
                default:
                    order = Sort.Order.asc(col);
                    break;
            }
            orders.add(order);
        }
        return Sort.by(orders);
    }

}
