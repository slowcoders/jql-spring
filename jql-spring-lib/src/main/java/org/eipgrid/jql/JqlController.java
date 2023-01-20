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

public interface JqlController<ENTITY, ID> {

    JqlRepository<ENTITY,ID> getRepository();

    class Search<ENTITY, ID> implements JqlController<ENTITY, ID> {
        private final JqlRepository<ENTITY, ID> repository;

        public Search(JqlRepository<ENTITY, ID> repository) {
            this.repository = repository;
        }

        public JqlRepository<ENTITY, ID> getRepository() {
            return repository;
        }

        @GetMapping(path = "/{id}")
        @ResponseBody
        @Operation(summary = "지정 엔터티 읽기")
        public ENTITY get(@PathVariable("id") String id$) {
            ID id = getRepository().convertId(id$);
            ENTITY entity = getRepository().find(id);
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
        public Object find(@Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                           @RequestBody JqlQuery.Request request) {
            return request.execute(getRepository());
        }

        @PostMapping(path = "/count")
        @ResponseBody
        @Operation(summary = "엔터티 수 조회")
        public long count(@Schema(implementation = Object.class)
                          @RequestBody() HashMap<String, Object> jsFilter) {
            long count = getRepository().count(getRepository().buildFilter(jsFilter));
            return count;
        }


        @PostMapping(path = "/clear-cache")
        @ResponseBody
        @Operation(summary = "Cache 비우기")
        public void clearCache() {
            getRepository().clearEntityCache(null);
        }
    }

    interface Insert<ENTITY, ID> extends JqlController<ENTITY, ID> {

        @PutMapping(path = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default ENTITY add(@RequestBody ENTITY entity) throws Exception {
            ID id = getRepository().insert(entity);
            return getRepository().find(id);
        }
    }

    interface Update<ENTITY, ID> extends JqlController<ENTITY, ID> {

        @PatchMapping(path = "/{idList}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @ResponseBody
        @Operation(summary = "엔터티 내용 변경")
        @Transactional
        default List<ENTITY> update(
                @Schema(type = "string", required = true) @PathVariable("idList") Collection<ID> idList,
                @RequestBody HashMap<String, Object> updateSet) throws Exception {
            getRepository().update(idList, updateSet);
            List<ENTITY> entities = getRepository().list(idList);
            return entities;
        }
    }

    interface Delete<ENTITY, ID> extends JqlController<ENTITY, ID> {
        @DeleteMapping("/{idList}")
        @ResponseBody
        @Operation(summary = "엔터티 삭제")
        @Transactional
        default Collection<ID> delete(@PathVariable("idList") Collection<ID> idList) {
            getRepository().delete(idList);
            return idList;
        }

    }


    /****************************************************************
     * Form Control API set
     */
    interface InsertForm<ENTITY, ID> extends JqlController<ENTITY, ID> {

        @PostMapping(path = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default ENTITY add_form(@ModelAttribute ENTITY entity) throws Exception {
            ID id = getRepository().insert(entity);
            return getRepository().find(id);
        }
    }

    class CRUD<ENTITY, ID> extends Search<ENTITY, ID>
            implements Insert<ENTITY, ID>, Update<ENTITY, ID>, Delete<ENTITY, ID> {
        public CRUD(JqlRepository<ENTITY, ID> repository) {
            super(repository);
        }
    }
}




