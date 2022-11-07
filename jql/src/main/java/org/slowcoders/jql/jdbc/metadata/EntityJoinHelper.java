package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.HashMap;

class EntityJoinHelper extends HashMap<JqlSchema, JqlEntityJoin> {
    private String tableName;
    public EntityJoinHelper(JqlSchema pkSchema) {
        this.tableName = pkSchema.getBaseTableName();
    }

    public JqlEntityJoin put(JqlSchema schema, JqlEntityJoin join) {
        JqlEntityJoin join2 = super.put(schema, join);
        if (join2 != null) {
            if (join.isJoinedBySingleKey() && join2.isJoinedBySingleKey()) {
                if (hasNameToken(join2.getJoinedColumns().get(0).getJsonKey(), tableName)) {
                    super.put(schema, join2);
                    return join2;
                } else if (hasNameToken(join.getJoinedColumns().get(0).getJsonKey(), tableName)) {
                    // do nothing;
                    return join2;
                }
            }
            throw new RuntimeException("Conflict Mapped Schema");
        }
        return join2;
    }

    private boolean hasNameToken(String jsKey, String tableName) {
        return jsKey.startsWith(tableName) && jsKey.charAt(tableName.length()) == '.';
    }
}
