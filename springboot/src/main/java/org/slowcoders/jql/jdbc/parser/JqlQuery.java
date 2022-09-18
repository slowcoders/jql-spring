package org.slowcoders.jql.jdbc.parser;

import org.slowcoders.jql.JqlColumnJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JqlSchemaJoin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JqlQuery extends QTableNode {

    private HashMap<String, JqlSchemaJoin> joinMap = new HashMap<>();
    private HashSet<String> fetchTables = new HashSet<>();

    public JqlQuery(JqlSchema schema) {
        super(schema);
    }

    protected void addTableJoin(JqlSchemaJoin join) {
        Object old = joinMap.put(join.getJoinedTable().getTableName(), join);
        if (old != null && old != join) {
            throw new RuntimeException("something wrong");
        }
    }

    public void writeJoinStatement(SQLWriter sb) {
        sb.write(this.getSchema().getTableName());
        for (Map.Entry<String, JqlSchemaJoin> e : joinMap.entrySet()) {
            String primaryTable = e.getKey();
            JqlSchemaJoin foreignKeys = e.getValue();
            sb.write("\nleft outer join ").write(primaryTable).write(" on\n");
            for (JqlColumnJoin fk : foreignKeys) {
                sb.write("  ").write(primaryTable).write(".").write(fk.getPkColumn());
                sb.write(" = ").write(fk.getFkTableName()).write(".").write(fk.getFkColumn()).write(" and\n");
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
