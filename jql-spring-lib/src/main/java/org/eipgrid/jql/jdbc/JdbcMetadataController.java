package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.JQColumn;
import org.eipgrid.jql.jdbc.metadata.JdbcSchema;
import org.eipgrid.jql.spring.JQRepository;
import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.js.JsUtil;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

public abstract class JdbcMetadataController {

    private final JdbcJQService service;

    public JdbcMetadataController(JdbcJQService service) {
        this.service = service;
    }


    private JQSchema getSchema(String db_schema, String tableName) throws Exception {
        String tablePath = service.makeTablePath(db_schema, tableName);
        JQRepository repo = service.makeRepository(tablePath);
        if (repo.getEntityType() != KVEntity.class) {
            return service.loadSchema(repo.getEntityType());
        }
        return service.loadSchema(tablePath, null);
    }

    @GetMapping("/{schema}/{table}")
    @ResponseBody
    @Operation(summary = "table column 목록")
    public List<String> columns(@PathVariable("schema") String db_schema,
                                @PathVariable("table") String tableName) throws Exception {
        JQSchema schema = getSchema(db_schema, tableName);
        ArrayList<String> columns = new ArrayList<>();
        for (JQColumn column : schema.getReadableColumns()) {
            columns.add(column.getJsonKey());
        }
        return columns;
    }


    @GetMapping("/{schema}/{table}/{type}")
    @ResponseBody
    @Operation(summary = "Schema 소스 생성")
    public String jsonSchema(@PathVariable("schema") String db_schema,
                             @PathVariable("table") String tableName,
                             @PathVariable("type") SchemaType type) throws Exception {
        if ("*".equals(tableName)) {
            return jpaSchemas(db_schema, type);
        }

        JQSchema schema = getSchema(db_schema, tableName);
        String source;
        if (type == SchemaType.Simple) {
            source = JsUtil.getSimpleSchema(schema);
        }
        else if (type == SchemaType.Javascript) {
            source = JsUtil.createDDL(schema);
            String join = JsUtil.createJoinJQL(schema);
            StringBuilder sb = new StringBuilder();
            sb.append(source).append("\n\n").append(join);
            source = sb.toString();
        }
        else {
            if (schema instanceof JdbcSchema) {
                source = ((JdbcSchema)schema).dumpJPAEntitySchema();
            }
            else {
                source = tableName + " is not a JdbcSchema";
            }
        }
        return source;
    }

    private String jpaSchemas(@PathVariable("schema") String db_schema,
                             @PathVariable("type") SchemaType type) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getTableNames(db_schema)) {
            JQSchema schema = getSchema(db_schema, tableName);
            if (type == SchemaType.Javascript) {
                String source = JsUtil.createDDL(schema);
                String join = JsUtil.createJoinJQL(schema);
                sb.append(source).append("\n\n").append(join);
            } else if (schema instanceof JdbcSchema) {
                String source = ((JdbcSchema) schema).dumpJPAEntitySchema();
                sb.append(source);
            } else {
                continue;
            }
            sb.append("\n\n//-------------------------------------------------//\n\n");
        }
        return sb.toString();
    }

    @GetMapping("/{schema}")
    @ResponseBody
    @Operation(summary = "Table 목록")
    public String listTables(@PathVariable("schema") String db_schema) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getTableNames(db_schema)) {
            sb.append(tableName).append('\n');
        }
        return sb.toString();
    }

    @GetMapping("/")
    @ResponseBody
    @Operation(summary = "DB schema 목록")
    public String listSchemas() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getDBSchemas()) {
            sb.append(tableName).append('\n');
        }
        return sb.toString();
    }

    enum SchemaType {
        Simple,
        Javascript,
        SpringJPA
    }
}
