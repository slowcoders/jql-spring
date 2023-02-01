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

public interface JqlStorageController {

    JqlEntityStore getRepository(String tableName);

    class Search implements JqlStorageController {
        private final JqlService service;
        private final String default_namespace;

        public Search(JqlService service, String default_namespace) {
            this.service = service;
            this.default_namespace = default_namespace;
        }

        public JqlEntityStore getRepository(String tableName) {
            String tablePath = service.makeTablePath(default_namespace, tableName);
            return service.getRepository(tablePath);
        }
        
        @GetMapping(path = "/{table}/{id}")
        @Operation(summary = "지정 엔터티 읽기")
        @Transactional
        @ResponseBody
        public Object get(@PathVariable("table") String table,
                          @PathVariable("id") String id$) {
            JqlEntityStore repository = getRepository(table);
            Object entity = repository.find(id$);
            if (entity == null) {
                throw new HttpServerErrorException("Entity(" + id$ + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
            }
            return entity;
        }

        @PostMapping(path = "/{table}/find", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
        @Operation(summary = "엔터티 검색")
        @Transactional
        @ResponseBody
        public Object find_form(@PathVariable("table") String table,
                                @Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                                @ModelAttribute JqlQuery.Request request) {
            return find(table, request);
        }

        @PostMapping(path = "/{table}/find", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 검색")
        @Transactional
        @ResponseBody
        public Object find(@PathVariable("table") String table,
                           @Schema(example = "{ \"select\": \"\", \"sort\": \"\", \"page\": 0, \"limit\": 0, \"filter\": { } }")
                           @RequestBody JqlQuery.Request request) {
            JqlEntityStore repository = getRepository(table);
            return request.buildQuery(repository).execute();
        }
    }
    
    interface ListAll extends JqlStorageController {

        @GetMapping(path = "/{table}")
        @Operation(summary = "전체 목록")
        @Transactional
        @ResponseBody
        default JqlQuery.Response list(@PathVariable("table") String table) throws Exception {
            JqlEntityStore repository = getRepository(table);
            return JqlQuery.of(repository, null, null).execute();
        }
    }


    interface Insert<ID> extends JqlStorageController {

        @PutMapping(path = "/{table}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 추가")
        @Transactional
        @ResponseBody
        default Object add(@PathVariable("table") String table,
                           @Schema(implementation = Object.class)
                           @RequestBody Map<String, Object> entity) throws Exception {
            JqlEntityStore repository = getRepository(table);
            Object id = repository.insert(entity);
            return repository.find(id);
        }
    }

    interface Update<ID> extends JqlStorageController {

        @PatchMapping(path = "/{table}/{idList}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 내용 변경")
        @Transactional
        @ResponseBody
        default List<?> update(@PathVariable("table") String table,
                               @Schema(type = "string", required = true) @PathVariable("idList") Collection<ID> idList,
                               @RequestBody HashMap<String, Object> updateSet) throws Exception {
            JqlEntityStore repository = getRepository(table);
            repository.update(idList, updateSet);
            List<?> entities = repository.find(idList);
            return entities;
        }
    }

    interface Delete<ID> extends JqlStorageController {
        @DeleteMapping("/{table}/{idList}")
        @Operation(summary = "엔터티 삭제")
        @Transactional
        @ResponseBody
        default Collection<ID> delete(@PathVariable("table") String table,
                                      @PathVariable("idList") Collection<ID> idList) {
            JqlEntityStore repository = getRepository(table);
            repository.delete(idList);
            return idList;
        }
    }

    class CRUD extends JqlStorageController.Search implements Insert, Update, Delete {
        public CRUD(JqlService service, String defaultNamespace) {
            super(service, defaultNamespace);
        }
    }

}
