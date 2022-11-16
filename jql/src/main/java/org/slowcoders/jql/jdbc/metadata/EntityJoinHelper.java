package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;

import java.util.HashMap;
import java.util.List;

class EntityJoinHelper extends HashMap<JqlSchema, List<JqlColumn>> {
    private String tableName;
    public EntityJoinHelper(JqlSchema pkSchema) {
        this.tableName = pkSchema.getSimpleTableName();
    }

    public List<JqlColumn> put(JqlSchema schema, List<JqlColumn> fkColumns) {
        List<JqlColumn> fkColumns2 = super.put(schema, fkColumns);
        if (fkColumns2 != null) {
            if (fkColumns.size() == 1 && fkColumns2.size() == 1) {
                if (hasNameToken(fkColumns2.get(0).getJsonKey(), tableName)) {
                    super.put(schema, fkColumns2);
                    return fkColumns2;
                } else if (hasNameToken(fkColumns.get(0).getJsonKey(), tableName)) {
                    // do nothing;
                    return fkColumns2;
                }
            }
            throw new RuntimeException("Conflict Mapped Schema");
        }
        return fkColumns2;
    }

    private boolean hasNameToken(String jsKey, String tableName) {
        return jsKey.startsWith(tableName) && jsKey.charAt(tableName.length()) == '.';
    }
}
