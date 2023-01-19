package org.eipgrid.jql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    /****************************************************************
     * Search API set
     */
    interface Search<ENTITY, ID> extends JqlController<ENTITY, ID> {

        @GetMapping(path = "/{id}")
        @ResponseBody
        @Operation(summary = "지정 엔터티 읽기")
        default ENTITY get(@PathVariable("id") String id$) {
            ID id = getRepository().convertId(id$);
            ENTITY entity = getRepository().find(id);
            if (entity == null) {
                throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
            }
            return entity;
        }

        @PostMapping(path = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 검색")
        default Object select_form(@Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"query\": { } }")
                                  @ModelAttribute JqlRequest.Builder request) {
            return select(request);
        }

        @PostMapping(path = "/", consumes = { MediaType.APPLICATION_JSON_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 검색")
        default Object select(@Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"query\": { } }")
                             @RequestBody JqlRequest.Builder request) {
            List<ENTITY> res = getRepository().select(request.build());
            return KVEntity.of("content", res);
        }

        @PostMapping(path = "/count")
        @ResponseBody
        @Operation(summary = "엔터티 수 조회")
        default long count(@Schema(implementation = Object.class)
                            @RequestBody() HashMap<String, Object> jsQuery) {
            long count = getRepository().count(jsQuery);
            return count;
        }


        @PostMapping(path = "/clear-cache")
        @ResponseBody
        @Operation(summary = "Cache 비우기")
        default void clearCache() {
            getRepository().clearEntityCache(null);
        }
    }


    /****************************************************************
     * Update API set
     */
    interface Update<ENTITY, ID> extends JqlController<ENTITY, ID> {

        @PutMapping(path = "/", consumes = { MediaType.APPLICATION_JSON_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default ENTITY add(@RequestBody ENTITY entity) throws Exception {
            ID id = getRepository().insert(entity);
            return getRepository().find(id);
        }

        @PatchMapping(path = "/{idList}", consumes = { MediaType.APPLICATION_JSON_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 내용 일부 변경")
        @Transactional
        default List<ENTITY> update(
                @Schema(type="string", required = true) @PathVariable("idList") Collection<ID> idList,
                @RequestBody HashMap<String, Object> updateSet) throws Exception {
            getRepository().update(idList, updateSet);
            List<ENTITY> entities = getRepository().list(idList);
            return entities;
        }

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
    interface Form<ENTITY, ID> extends JqlController<ENTITY, ID> {

        @PostMapping(path = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default ENTITY add_form(@ModelAttribute ENTITY entity) throws Exception {
            ID id = getRepository().insert(entity);
            return getRepository().find(id);
        }

//        @PatchMapping(path = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
//        @ResponseBody
//        @Operation(summary = "엔터티 내용 일부 변경")
//        @Transactional
//        default List<ENTITY> update(
//                @Schema(type="string", required = true) @PathVariable("id") ID id,
//                @ModelAttribute ENTITY entity) throws Exception {
//            getRepository().update(idList, updateSet);
//            List<ENTITY> entities = getRepository().list(idList);
//            return entity;
//        }
    }

    abstract class SearchAndUpdate<ENTITY, ID> implements Search<ENTITY, ID>, Update<ENTITY, ID> {
        JqlRepository<ENTITY, ID> repository;

        public SearchAndUpdate(JqlRepository<ENTITY, ID> repository) {
            this.repository = repository;
        }

        @Override
        public JqlRepository<ENTITY, ID> getRepository() {
            return repository;
        }

    }
}




