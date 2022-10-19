package org.slowcoders.jql.jdbc;//package org.slowcoders.jql.jdbc;
//
//import io.swagger.v3.oas.annotations.Operation;
//import org.slowcoders.genorm.JqlSchema;
//import org.springframework.context.annotation.Profile;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Profile("jql")
//@RestController
//@RequestMapping(value = "/api/jql/util")
//public class JdbcSchemaController {
//
//    private JdbcJQLService service;
//
//    public JdbcSchemaController(JdbcJQLService service) {
//        super();
//        this.service = service;
//    }
//
//    private JqlSchema getSchema(String dbSchema, String tableName) throws Exception {
//        return service.getMetadataProcessor().loadTableInfo(dbSchema, tableName);
//    }
//
//    private List<String> getAllTableNames(String dbSchema) throws Exception {
//        return service.getMetadataProcessor().getTableNames(dbSchema);
//    }
//
//    @GetMapping("/{schema}/{table}/gen-jpa")
//    @ResponseBody
//    @Operation(summary = "JPA Entity 소스 생성")
//    public String jpaSchema(@PathVariable("schema") String dbSchema,
//                            @PathVariable("table") String tableName) throws Exception {
//        JqlSchema schema = getSchema(dbSchema, tableName);
//        String source = schema.dumpJPAEntitySchema();
//        return source;
//    }
//
//    @GetMapping("/{schema}/gen-jpa")
//    @ResponseBody
//    @Operation(summary = "JPA Entity 소스 생성 (전체)")
//    public String jpaSchemas(@PathVariable("schema") String dbSchema) throws Exception {
//        StringBuilder sb = new StringBuilder();
//        for (String tableName : this.getAllTableNames(dbSchema)) {
//            JqlSchema schema = getSchema(dbSchema, tableName);
//            sb.append("\n\n//-------------------------------------------------//\n\n");
//            String source = schema.dumpJPAEntitySchema();
//            sb.append(source);
//        }
//        return sb.toString();
//    }
//
//
//    @GetMapping("/{schema}/{table}/gen-json")
//    @ResponseBody
//    @Operation(summary = "JSON Schema 소스 생성")
//    public String mdkSchema(@PathVariable("schema") String dbSchema,
//                            @PathVariable("table") String tableName) throws Exception {
//        JqlSchema schema = getSchema(dbSchema, tableName);
//        String source = schema.generateDDL();
//        return source;
//    }
//
//    @GetMapping("/{schema}/gen-json")
//    @ResponseBody
//    @Operation(summary = "JSON Schema 소스 생성 (전체)")
//    public String jsonSchemas(@PathVariable("schema") String dbSchema) throws Exception {
//        StringBuilder sb = new StringBuilder();
//        for (String tableName : this.getAllTableNames(dbSchema)) {
//            JqlSchema schema = getSchema(dbSchema, tableName);
//            sb.append("\n\n//-------------------------------------------------//\n\n");
//            String source = schema.generateDDL();
//            sb.append(source);
//        }
//        return sb.toString();
//    }
//
//    @GetMapping("/{schema}/list-table")
//    @ResponseBody
//    @Operation(summary = "DBSchema 내 Table list")
//    public String listTables(@PathVariable("schema") String dbSchema) throws Exception {
//        StringBuilder sb = new StringBuilder();
//        for (String tableName : this.getAllTableNames(dbSchema)) {
//            JqlSchema schema = getSchema(dbSchema, tableName);
//            sb.append("\n\n//-------------------------------------------------//\n\n");
//            String source = schema.generateDDL();
//            sb.append(source);
//        }
//        return sb.toString();
//    }
//
//    @GetMapping("/list-schema")
//    @ResponseBody
//    @Operation(summary = "DBSchema 내 Table list")
//    public String listSchemas() throws Exception {
//        StringBuilder sb = new StringBuilder();
//        for (String tableName : service.getMetadataProcessor().getDBSchemas()) {
//            sb.append(tableName).append('\n');
//        }
//        return sb.toString();
//    }
//}
