package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class TableQuery extends EntityQuery {
    private final JqlSchema schema;

    public TableQuery(EntityQuery parentQuery, JqlSchema schema) {
        super(parentQuery);
        this.schema = schema;
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public TableQuery asTableQuery() {
        return this;
    }

    public String getTableName() { return schema.getTableName(); }

    public TableQuery getTableQuery() {
        return this;
    }

    @Override
    public EntityQuery getQueryScope_impl(String key, Type isLeaf_unused, boolean fetchData) {
        JqlEntityJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != JsonNodeType.Object) return this;
        }

        EntityQuery subQuery = subQueries.get(key);
        if (subQuery == null) {
            if (join != null) {
                JqlSchema subSchema = getTopQuery().addTableJoin(key, join, fetchData);
                subQuery = new TableQuery(this, subSchema);
            }
            else {
                subQuery = new JsonQuery(this, key);
            }
            subQueries.put(key, subQuery);
        }
        return subQuery;
    }

    @Override
    public void writeAttribute(SqlBuilder sb, String key, Class<?> valueType) {
        sb.write(getSchema().getTableName()).write(".").write(key);
    }

    @Override
    public void accept(JqlVisitor sb) {
        JqlSchema old = sb.setWorkingSchema(this.schema);
        super.accept(sb);
        sb.setWorkingSchema(old);
    }

    @Override
    public String getColumnName(String key) {
        while (!this.schema.hasColumn(key)) {
            int p = key.indexOf('.');
            if (p < 0) {
                throw new IllegalArgumentException("invalid key: " + key);
            }
            key = key.substring(p + 1);
        }
        return this.schema.getColumn(key).getColumnName();
    }
}
