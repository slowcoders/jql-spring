package org.slowcoders.jql;

import java.util.ArrayList;

public class JqlSchemaJoin extends ArrayList<JqlColumnJoin> {
    private final SchemaLoader schemaLoader;
    private JqlSchema joinedSchema;

    public JqlSchemaJoin(SchemaLoader schemaLoader) {
        this.schemaLoader = schemaLoader;
    }

    public JqlSchema getJoinedTable() {
        if (joinedSchema == null) {
            JqlColumnJoin fk = super.get(0);
            this.joinedSchema = schemaLoader.loadSchema(fk.getPkTableName());
        }
        return this.joinedSchema;
    }
}
