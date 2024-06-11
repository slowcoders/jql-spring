package org.slowcoders.hyperql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.transaction.Transactional;
import org.slowcoders.hyperql.js.JsUtil;
import org.slowcoders.hyperql.schema.QSchema;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

public interface HyperStorageController extends RestTemplate {

    EntitySet getEntitySet(String tableName);

    class Search implements HyperStorageController {
        private final HyperStorage storage;
        private final String tableNamePrefix;
        private final ConversionService conversionService;

        public Search(HyperStorage storage, String namespace, ConversionService conversionService) {
            this.storage = storage;
            this.tableNamePrefix = namespace == null || namespace.length() == 0 ? "" : namespace + '.';
            this.conversionService = conversionService;
        }

        public final HyperStorage getStorage() {
            return storage;
        }

        public EntitySet getEntitySet(String tableName) {
            String tablePath = tableNamePrefix + tableName;
            return storage.loadEntitySet(tablePath);
        }

        @GetMapping(path = "/schemas")
        @Operation(summary = "전체 Schema")
        @ResponseBody
        public String listSchemas() {
            List<String> tableNames = listTableNames();
            StringBuilder sb = new StringBuilder();
            for (String table: tableNames) {
                QSchema schema1 = storage.loadSchema(tableNamePrefix + table);
                String simpleSchema = JsUtil.getSimpleSchema(schema1, true);
                sb.append(simpleSchema);
                sb.append('\n');
            }
            return sb.toString();
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
            EntitySet entities = getEntitySet(table);
            HyperSelect select = HyperSelect.of(select$);
            Object res = entities.find(id, select);
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
                OutputOptions req,
                @Schema(implementation = Object.class)
                @RequestBody Map<String, Object> filter) throws Exception {
            EntitySet entities = getEntitySet(table);
            return search(entities, req, filter);
        }

        @PostMapping(path = "/{table}/count")
        @Operation(summary = "엔터티 수 조회")
        @Transactional
        @ResponseBody
        public long count(
                @PathVariable("table") String table,
                @Schema(implementation = Object.class)
                @RequestBody HashMap<String, Object> jsFilter) throws Exception {
            EntitySet entities = getEntitySet(table);
            long count = entities.createQuery(jsFilter).count();
            return count;
        }

//        @PostMapping("/{table}/schema")
//        @ResponseBody
//        @Operation(summary = "엔터티 속성 정보 요약")
//        public String schema(@PathVariable("table") String table) {
//            JqlEntitySet entities = getRepository(table);
//            String schema = JsUtil.getSimpleSchema(entities.getSchema());
//            return schema;
//        }
    }

    interface ListAll extends HyperStorageController {

        @GetMapping(path = "/{table}")
        @Operation(summary = "전체 목록")
        @Transactional
        @ResponseBody
        default Response list(@PathVariable("table") String table,
                              OutputOptions params) throws Exception {
            EntitySet entities = getEntitySet(table);
            return search(entities, params, null);
        }
    }


    interface Insert extends HyperStorageController {

        @PutMapping(path = "/{table}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 추가")
        @Transactional
        @ResponseBody
        default Response add(
                @PathVariable("table") String table,
                @RequestParam(value = "select", required = false) String select$,
                @Schema(implementation = Object.class)
                @RequestBody Map<String, Object> properties) throws Exception {
            EntitySet entities = getEntitySet(table);
            Object id = entities.insert(properties);
            if (select$ != null) {
                HyperSelect select = HyperSelect.of(select$);
                Object createdEntity = entities.find(id, select);
                return Response.of(createdEntity, select);
            } else {
                return Response.of("inserted", id);
            }
        }

        @PutMapping(path = "/{table}/add-all", consumes = {MediaType.APPLICATION_JSON_VALUE})
        @Operation(summary = "엔터티 추가")
        @Transactional
        @ResponseBody
        default Response addAll(
                @PathVariable("table") String table,
                @RequestParam(value = "select", required = false) String select$,
                @RequestParam(value = "onConflict", required = false) String onConflict,
                @Schema(implementation = Object.class)
                @RequestBody List<Map<String, Object>> entities) throws Exception {
            EntitySet.InsertPolicy insertPolicy = parseInsertPolicy(onConflict);
            EntitySet entitySet = getEntitySet(table);
            List idList = entitySet.insert(entities, insertPolicy);
            if (select$ != null) {
                HyperSelect select = HyperSelect.of(select$);
                List<?> createdEntities = entitySet.find(idList, select);
                return Response.of(createdEntities, select);
            } else {
                return Response.of("inserted", idList.size());
            }
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
            EntitySet entities = getEntitySet(table);
            idList = entities.convertIdList(idList);
            entities.update(idList, properties);
            List<ENTITY> res = entities.find(idList, select);
            return res;
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
            EntitySet entities = getEntitySet(table);
            entities.delete(entities.convertIdList(idList));
            return idList;
        }
    }

    class CRUD extends HyperStorageController.Search implements Insert, Update, Delete {
        public CRUD(HyperStorage storage, String tableNamePrefix, ConversionService conversionService) {
            super(storage, tableNamePrefix, conversionService);
        }
    }

}
