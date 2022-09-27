package org.slowcoders.jql.jdbc.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlColumnJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JqlSchemaJoin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class JqlQuery extends QTableNode {

    private HashMap<String, List<JqlColumn>> joinMap = new HashMap<>();
    private HashSet<String> fetchTables = new HashSet<>();

    public JqlQuery(JqlSchema schema) {
        super(schema);
    }

    protected QTableNode addTableJoin(List<JqlColumn> foreignKeys) {
        JqlSchema pkSchema = foreignKeys.get(0).getJoinedPrimaryColumn().getSchema();
        Object old = joinMap.put(pkSchema.getTableName(), foreignKeys);
        if (old != null && old != foreignKeys) {
            throw new RuntimeException("something wrong");
        }
        return new QTableNode(pkSchema);
    }

    public void writeJoinStatement(SQLWriter sb) {
        sb.write(this.getSchema().getTableName());
        for (Map.Entry<String, List<JqlColumn>> e : joinMap.entrySet()) {
            String primaryTable = e.getKey();
            List<JqlColumn> foreignKeys = e.getValue();
            sb.write("\nleft outer join ").write(primaryTable).write(" on\n");
            for (JqlColumn fk : foreignKeys) {
                sb.write("  ").write(primaryTable).write(".").write(fk.getJoinedPrimaryColumn().getColumnName());
                sb.write(" = ").write(fk.getSchema().getTableName()).write(".")
                        .write(fk.getColumnName()).write(" and\n");
            }
            sb.shrinkLength(5);
        }
    }

    public void writeSelect(SQLWriter sb) {
        sb.write("SELECT ").write(getSchema().getTableName()).write(".* ");
        for (String table : fetchTables) {
            sb.write(", ").write(table).write(".* ");
        }
    }

    public void markFetchData(JqlSchema tableName) {
        this.fetchTables.add(tableName.getTableName());
    }
}
