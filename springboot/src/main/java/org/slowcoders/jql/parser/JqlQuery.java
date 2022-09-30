package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class JqlQuery extends EntityFilter {

    private HashMap<String, List<JqlColumn>> joinMap = new HashMap<>();
    private HashSet<String> fetchTables = new HashSet<>();

    public JqlQuery(JqlSchema schema) {
        super(schema);
    }

    protected void markFetchData(JqlSchema tableName) {
        this.fetchTables.add(tableName.getTableName());
    }

    protected EntityFilter addTableJoin(List<JqlColumn> foreignKeys) {
        JqlSchema pkSchema = foreignKeys.get(0).getJoinedPrimaryColumn().getSchema();
        Object old = joinMap.put(pkSchema.getTableName(), foreignKeys);
        if (old != null && old != foreignKeys) {
            throw new RuntimeException("something wrong");
        }
        return new EntityFilter(pkSchema);
    }

    protected void writeJoinStatement(QueryBuilder sb) {
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


    public Iterable<String> getFetchTables() {
        return fetchTables;
    }
}
