package org.slowcoders.hyperql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface HyperStorageController extends RestTemplate {

    EntitySet getEntitySet(String tableName);

    class Search implements HyperStorageController {
        private final HyperStorage storage;
        private final String tableNamePrefix;
        private final ConversionService conversionService;

        public Search(HyperStorage storage, String tableNamePrefix, ConversionService conversionService) {
            this.storage = storage;
            this.tableNamePrefix = tableNamePrefix == null ? "" : tableNamePrefix;
            this.conversionService = conversionService;
        }

        public final HyperStorage getStorage() {
            return storage;
        }

        public EntitySet getEntitySet(String tableName) {
            String tablePath = tableNamePrefix + tableName;
            return storage.loadEntitySet(tablePath);
        }

        @GetMapping(path = "/")
        @Operation(summary = "Table 목록")
        @ResponseBody
        public List<String> listTableNames() {
            int p = tableNamePrefix.indexOf('.');
            String namespace = p <= 0 ? null : tableNamePrefix.substring(0, p);
            List<String> tableNames = storage.getTableNames(namespace);
            return tableNames;
        }

        @GetMapping(path = "/{table}/{id}")
        @Operation(summary = "지정 엔터티 읽기")
        @Transactional
        @ResponseBody
        public Response get(
                @PathVariable("table") String table,
                @Schema(implementation = String.class)
                @PathVariable("id") Object id,
                @RequestParam(value = "select", required = false) String select$) {
            EntitySet enitities = getEntitySet(table);
            HyperSelect select = HyperSelect.of(select$);
            Object res = enitities.find(id, select);
            if (res == null) {
                throw new HttpServerErrorException("Entity(" + id + ") is not found", HttpStatus.NOT_FOUND, null, null, null, null);
            }
            return Response.of(res, select);
        }

        @PostMapping(path = "/{table}/find", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 검색")
        @Transactional
        @ResponseBody
        public Response find(
                @PathVariable("table") String table,
                @RequestParam(value = "select", required = false) String select,
                @Schema(implementation = String.class)
                @RequestParam(value = "sort", required = false) String[] orders,
                @RequestParam(value = "page", required = false) Integer page,
                @RequestParam(value = "limit", required = false) Integer limit,
                @Schema(implementation = Object.class)
                @RequestBody Map<String, Object> filter) {
            EntitySet enitities = getEntitySet(table);
            return search(enitities, select, orders, page, limit, filter);
        }

        @PostMapping(path = "/{table}/count")
        @Operation(summary = "엔터티 수 조회")
        @Transactional
        @ResponseBody
        public long count(
                @PathVariable("table") String table,
                @Schema(implementation = Object.class)
                @RequestBody HashMap<String, Object> jsFilter) {
            EntitySet enitities = getEntitySet(table);
            long count = enitities.createQuery(jsFilter).count();
            return count;
        }

//        @PostMapping("/{table}/schema")
//        @ResponseBody
//        @Operation(summary = "엔터티 속성 정보 요약")
//        public String schema(@PathVariable("table") String table) {
//            JqlEntitySet enitities = getRepository(table);
//            String schema = JsUtil.getSimpleSchema(enitities.getSchema());
//            return schema;
//        }
    }

    interface ListAll extends HyperStorageController {

        @GetMapping(path = "/{table}")
        @Operation(summary = "전체 목록")
        @Transactional
        @ResponseBody
        default Response list(
                @PathVariable("table") String table,
                @RequestParam(value = "select", required = false) String select,
                @Schema(implementation = String.class)
                @RequestParam(value = "sort", required = false) String[] orders,
                @RequestParam(value = "page", required = false) Integer page,
                @RequestParam(value = "limit", required = false) Integer limit) throws Exception {
            EntitySet enitities = getEntitySet(table);
            return search(enitities, select, orders, page, limit, null);
        }
    }


    interface Insert extends HyperStorageController {

        @PutMapping(path = "/{table}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 추가")
        @Transactional
        @ResponseBody
        default <ENTITY> ENTITY add(
                @PathVariable("table") String table,
                @Schema(implementation = Object.class)
                @RequestBody Map<String, Object> properties) throws Exception {
            EntitySet enitities = getEntitySet(table);
            Object created = enitities.insert(properties);
            return (ENTITY) created;
        }

        @PutMapping(path = "/{table}/add-all", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 추가")
        @Transactional
        @ResponseBody
        default <ID> List<ID> addAll(
                @PathVariable("table") String table,
                @RequestParam(value = "onConflict", required = false) String onConflict,
                @Schema(implementation = Object.class)
                @RequestBody List<Map<String, Object>> entities) throws Exception {
            EntitySet.InsertPolicy insertPolicy = parseInsertPolicy(onConflict);
            EntitySet enitities = getEntitySet(table);
            List<ID> res = (List<ID>)enitities.insert(entities, insertPolicy);
            return res;
        }

    }

    interface Update extends HyperStorageController {

        @PatchMapping(path = "/{table}/{idList}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 내용 변경")
        @Transactional
        @ResponseBody
        default <ENTITY, ID> Collection<ENTITY> update(
                @PathVariable("table") String table,
                @Schema(type = "string", required = true)
                @PathVariable("idList") Collection<ID> idList,
                @RequestParam(value = "select", required = false) String select$,
                @Schema(implementation = Object.class)
                @RequestBody Map<String, Object> properties) throws Exception {
            HyperSelect select = HyperSelect.of(select$);
            EntitySet enitities = getEntitySet(table);
            enitities.update(idList, properties);
            List<ENTITY> res = enitities.find(idList, select);
            return (Collection<ENTITY>) Response.of(res, select);
        }
    }

    interface Delete extends HyperStorageController {
        @DeleteMapping("/{table}/{idList}")
        @Operation(summary = "엔터티 삭제")
        @Transactional
        @ResponseBody
        default <ID> Collection<ID> delete(
                @PathVariable("table") String table,
                @Schema(implementation = String.class)
                @PathVariable("idList") Collection<ID> idList) {
            EntitySet enitities = getEntitySet(table);
            enitities.delete(idList);
            return idList;
        }
    }

    class CRUD extends HyperStorageController.Search implements Insert, Update, Delete {
        public CRUD(HyperStorage storage, String tableNamePrefix, ConversionService conversionService) {
            super(storage, tableNamePrefix, conversionService);
        }
    }

}
