package org.eipgrid.jql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface JqlController<ID> {

    JqlEntityStore<ID> getStore();

    class Search<ID> implements JqlController<ID> {
        private final JqlEntityStore<ID> store;

        public Search(JqlEntityStore<ID> store) {
            this.store = store;
        }

        public JqlEntityStore<ID> getStore() {
            return store;
        }

        @GetMapping(path = "/{id}")
        @ResponseBody
        @Operation(summary = "지정 엔터티 읽기")
        public Object get(@PathVariable("id") ID id) {
            Object entity = getStore().find(id);
            if (entity == null) {
                throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
            }
            return entity;
        }

        @PostMapping(path = "/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
        @ResponseBody
        @Operation(summary = "엔터티 검색")
        public Object find_form(@Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                                @ModelAttribute JqlQuery.Request request) {
            return find(request);
        }

        @PostMapping(path = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @ResponseBody
        @Operation(summary = "엔터티 검색")
        public JqlQuery.Response find(@Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                           @RequestBody JqlQuery.Request request) {
            return request.buildQuery(getStore()).execute();
        }

        @PostMapping(path = "/count")
        @ResponseBody
        @Operation(summary = "엔터티 수 조회")
        public long count(@Schema(implementation = Object.class)
                          @RequestBody() HashMap<String, Object> jsFilter) {
            long count = getStore().count(getStore().createFilter(jsFilter));
            return count;
        }


        @PostMapping(path = "/clear-cache")
        @ResponseBody
        @Operation(summary = "Cache 비우기")
        public void clearCache() {
            getStore().clearEntityCaches();
        }
    }

    interface Insert<ID> extends JqlController<ID> {

        @PutMapping(path = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default Object add(@RequestBody Map<String, Object> entity) throws Exception {
            ID id = getStore().insert(entity);
            return getStore().find(id);
        }
    }

    interface Update<ID> extends JqlController<ID> {

        @PatchMapping(path = "/{idList}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @ResponseBody
        @Operation(summary = "엔터티 내용 변경")
        @Transactional
        default List<?> update(
                @Schema(type = "string", required = true) @PathVariable("idList") Collection<ID> idList,
                @RequestBody HashMap<String, Object> updateSet) throws Exception {
            getStore().update(idList, updateSet);
            List<?> entities = getStore().find(idList);
            return entities;
        }
    }

    interface Delete<ID> extends JqlController<ID> {
        @DeleteMapping("/{idList}")
        @ResponseBody
        @Operation(summary = "엔터티 삭제")
        @Transactional
        default Collection<ID> delete(@PathVariable("idList") Collection<ID> idList) {
            getStore().delete(idList);
            return idList;
        }
    }


    /****************************************************************
     * Form Control API set
     */
    interface InsertForm<ENTITY, ID> extends JqlController<ID> {

        @PostMapping(path = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default ENTITY add_form(@ModelAttribute ENTITY entity) throws Exception {
            throw new RuntimeException("not implemented!");
//            ID id = getRepository().insert(entity);
//            return getRepository().find(id);
        }
    }

    class CRUD<ID> extends Search<ID> implements Insert<ID>, Update<ID>, Delete<ID> {
        public CRUD(JqlEntityStore<ID> store) {
            super(store);
        }
    }

}




