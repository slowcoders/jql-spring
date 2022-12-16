package org.eipgrid.jql.spring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.JqlSelect;
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

public interface JQLController<ENTITY, ID> {

    JQLRepository<ENTITY,ID> getRepository();

    /****************************************************************
     * Search API set
     */
    interface Search<ENTITY, ID> extends JQLController<ENTITY, ID> {

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

        @GetMapping(path = "/")
        @ResponseBody
        @Operation(summary = "전체 엔터티 리스트")
        default Object list(@RequestParam(value = "page", required = false) int page,
                            @Parameter(name = "limit", example = "10")
                            @RequestParam(value = "limit", defaultValue = "0") int limit,
                            @RequestParam(value = "select", required = false) String columns,
                            @RequestParam(value = "sort", required = false) String sort) {
            return find(page, limit, sort, columns, null);
        }

        @PostMapping(path = "/find")
        @ResponseBody
        @Operation(summary = "조건 검색")
        default Object find(@RequestParam(value = "page", defaultValue = "-1") int page,
                            @Parameter(name = "limit", example = "10")
                            @RequestParam(value = "limit", defaultValue = "0") int limit,
                            @RequestParam(value = "select", required = false) String columns,
                            @RequestParam(value = "sort", required = false) String sort,
                            @Schema(implementation = Object.class)
                            @RequestBody() HashMap<String, Object> filter) {
            boolean need_pagination = page >= 0 && limit > 1;
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
        default ENTITY top(@RequestParam(value = "select", required = false) String columns,
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


    /****************************************************************
     * Update API set
     */
    interface Update<ENTITY, ID> extends JQLController<ENTITY, ID> {

        @PostMapping(path = "/", consumes = { MediaType.APPLICATION_JSON_VALUE })
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
    interface Form<ENTITY, ID> extends JQLController<ENTITY, ID> {

        @PostMapping(path = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @ResponseBody
        @Operation(summary = "엔터티 추가")
        @Transactional
        default ENTITY add_form(@ModelAttribute ENTITY entity) throws Exception {
            ID id = getRepository().insert(entity);
            return getRepository().find(id);
        }

        @PatchMapping(path = "/{idList}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
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
    }

    abstract class SearchAndUpdate<ENTITY, ID> implements Search<ENTITY, ID>, Update<ENTITY, ID> {}
}




