package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlEntityJoin;

import java.util.HashMap;

class EntityJoinHelper extends HashMap<JqlSchema, JqlEntityJoin> {
    private String tableName;
    public EntityJoinHelper(JqlSchema pkSchema) {
        this.tableName = pkSchema.getSimpleTableName();
    }

    public JqlEntityJoin put(JqlSchema schema, JqlEntityJoin childJoin) {
        JqlEntityJoin oldJoin = super.put(schema, childJoin);
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
