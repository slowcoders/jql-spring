package org.slowcoders.jql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class JqlColumnJoin {
    @JsonIgnore
    private final String pkTableName;
    private final String pkColumn;

    private final String fkTableName;
    private final String fkColumn;

    private final SchemaLoader schemaLoader;

    public JqlColumnJoin(String pkTableName, String pkColumn, String fkTableName, String fkColumn, SchemaLoader schemaLoader) {
        this.pkTableName = pkTableName;
        this.pkColumn = pkColumn;
        this.fkTableName = fkTableName;
        this.fkColumn = fkColumn;
        this.schemaLoader = schemaLoader;
    }

    public final JqlSchema loadPkSchema() {
        return schemaLoader.loadSchema(pkTableName);
    }

    public String getJoinedFieldName() {
        return loadPkSchema().getJpaClassName();
    }
}
