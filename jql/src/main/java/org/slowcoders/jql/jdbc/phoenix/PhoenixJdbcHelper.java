package org.slowcoders.jql.jdbc.phoenix;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.SchemaLoader;
import org.slowcoders.jql.jdbc.SqlGenerator;
import org.slowcoders.jql.parser.SqlWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;

public class PhoenixJdbcHelper extends SqlGenerator {
    protected final JdbcTemplate jdbc;
    private final Class<?> entityType;
    private final String tableName;
    private static SchemaLoader schemaLoader = new PhoenixSchemaLoader();

    ArrayList<JqlColumn> columns = new ArrayList<>();

    public PhoenixJdbcHelper(JdbcTemplate jdbc, Class<?> entityType, String tableName) {
        super(
                schemaLoader.loadSchema(entityType, tableName));
        this.jdbc = jdbc;
        this.entityType = entityType;
        this.tableName = tableName;

    }

    @Override
    protected String getCommand(SqlWriter.Command command) {
        switch (command) {
            case Insert: case Update:
                return "UPSERT";
        }
        return super.getCommand(command);
    }


}
