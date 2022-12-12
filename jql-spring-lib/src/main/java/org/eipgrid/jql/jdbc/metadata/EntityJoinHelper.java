package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSchemaJoin;

import java.util.HashMap;
import java.util.List;

class SchemaJoinHelper extends HashMap<JqlSchema, JqlSchemaJoin> {
    private String tableName;
    public SchemaJoinHelper(JqlSchema pkSchema) {
        this.tableName = pkSchema.getSimpleTableName();
    }

    public JqlSchemaJoin put(JqlSchema schema, JqlSchemaJoin childJoin) {
        JqlSchemaJoin oldJoin = super.put(schema, childJoin);
        if (oldJoin != null && oldJoin != childJoin) {
            if (oldJoin.getJsonKey().equals(tableName)) {
                super.put(schema, oldJoin);
                return oldJoin;
            } else if (childJoin.getJsonKey().equals(tableName)) {
                // do nothing;
                return oldJoin;
            }
            throw new RuntimeException("Conflict Mapped Schema");
        }
        return oldJoin;
    }
}
