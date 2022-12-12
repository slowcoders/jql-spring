package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.jdbc.metadata.JdbcSchema;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.json.JsonJql;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

public abstract class JdbcDatabaseController {

    private final JQLJdbcService service;

    public JdbcDatabaseController(JQLJdbcService service) {
        this.service = service;
    }


    private JqlSchema getSchema(String db_schema, String tableName) throws Exception {
        String tablePath = service.makeTablePath(db_schema, tableName);
        JQLRepository repo = service.makeRepository(tablePath);
        if (repo.getEntityType() != KVEntity.class) {
            return service.loadSchema(repo.getEntityType());
        }
        return service.loadSchema(tablePath);
    }

    @GetMapping("/jpa-schema/{schema}/{table}")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성")
    public String jpaSchema(@PathVariable("schema") String db_schema,
                            @PathVariable("table") String tableName) throws Exception {
        JqlSchema schema = getSchema(db_schema, tableName);
        String source;
        if (schema instanceof JdbcSchema) {
            source = ((JdbcSchema)schema).dumpJPAEntitySchema();
        }
        else {
            source = tableName + " is not a JdbcSchema";
        }
        return source;
    }

    @GetMapping("/jpa-schema/{schema}/{table}/columns")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성")
    public List<String> columns(@PathVariable("schema") String db_schema,
                                @PathVariable("table") String tableName) throws Exception {
        JqlSchema schema = getSchema(db_schema, tableName);
        ArrayList<String> columns = new ArrayList<>();
        for (JqlColumn column : schema.getReadableColumns()) {
            columns.add(column.getJsonKey());
        }
        return columns;
    }

    @GetMapping("/jpa-schema/{schema}/")
    @ResponseBody
    @Operation(summary = "JPA Entity 소스 생성 (전체 Table)")
    public String jpaSchemas(@PathVariable("schema") String db_schema) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getTableNames(db_schema)) {
            sb.append("\n\n//-------------------------------------------------//\n\n");
            String source = this.jpaSchema(db_schema, tableName);
            sb.append(source);
        }
        return sb.toString();
    }


    @GetMapping("/json-schema/{schema}/{table}")
    @ResponseBody
    @Operation(summary = "JSON Schema 소스 생성")
    public String jsonSchema(@PathVariable("schema") String db_schema,
                             @PathVariable("table") String tableName) throws Exception {
        JqlSchema schema = getSchema(db_schema, tableName);
        String source = JsonJql.createDDL(schema);
        String join = JsonJql.createJoinJQL(schema);
        StringBuilder sb = new StringBuilder();
        sb.append(source).append("\n\n").append(join);
        return sb.toString();
    }

    @GetMapping("/json-schema/{schema}/")
    @ResponseBody
    @Operation(summary = "JSON Schema 소스 생성 (전체 Table)")
    public String jsonSchemas(@PathVariable("schema") String db_schema) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getTableNames(db_schema)) {
            JqlSchema schema = getSchema(db_schema, tableName);
            sb.append("\n\n//-------------------------------------------------//\n\n");
            String source = JsonJql.createDDL(schema);
            String join = JsonJql.createJoinJQL(schema);
            sb.append(source).append("\n\n").append(join);
            sb.append(source);
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
    @Operation(summary = "DBSchema 목록")
    public String listSchemas() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String tableName : service.getDBSchemas()) {
            sb.append(tableName).append('\n');
        }
        return sb.toString();
    }

}
