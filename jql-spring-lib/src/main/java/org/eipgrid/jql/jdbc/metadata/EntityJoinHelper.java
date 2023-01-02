package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.JQJoin;

import java.util.HashMap;

class EntityJoinHelper extends HashMap<JQSchema, JQJoin> {
    private String tableName;
    public EntityJoinHelper(JQSchema pkSchema) {
        this.tableName = pkSchema.getSimpleTableName();
    }

    public JQJoin put(JQSchema schema, JQJoin childJoin) {
        JQJoin oldJoin = super.put(schema, childJoin);
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
