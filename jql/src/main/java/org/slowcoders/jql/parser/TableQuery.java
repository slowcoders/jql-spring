package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

import java.util.ArrayList;

class TableQuery extends EntityQuery {
    private final JqlSchema schema;

    private final ArrayList<TableQuery> joinedTables = new ArrayList<>();
    private final JqlEntityJoin join;

    protected TableQuery(TableQuery parentQuery, JqlSchema schema) {
        this(null, schema, null, true);
    }

    public TableQuery(TableQuery parentQuery, JqlSchema schema, JqlEntityJoin join, boolean fetchData) {
        super(parentQuery);
        if (parentQuery != null) {// && parentQuery.joinedTables.indexOf(join) < 0) {
            parentQuery.joinedTables.add(this);
        }
        this.schema = schema;
        this.join = join;
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
                JqlSchema subSchema = getTopQuery().addTableJoin(join, fetchData);
                subQuery = new TableQuery(this, subSchema, join, fetchData);
            }
            else {
                subQuery = new JsonQuery(this, key);
            }
            subQueries.put(key, subQuery);
        }
        return subQuery;
    }

    @Override
    public void writeAttribute(SourceWriter sb, String key, Class<?> valueType) {
        sb.write(getSchema().getTableName()).write(".").write(key);
    }

    @Override
    public void accept(JqlPredicateVisitor visitor) {
        JqlSchema old = visitor.setWorkingSchema(this.schema);
        super.accept(visitor);
        visitor.setWorkingSchema(old);
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

    public JqlEntityJoin getEntityJoin() {
        return this.join;
    }

    public void accept(JqlEntityJoinVisitor jqlEntityJoinVisitor) {
        for (TableQuery table : joinedTables) {
            jqlEntityJoinVisitor.visitJoinedSchema(table);
        }
    }
}
