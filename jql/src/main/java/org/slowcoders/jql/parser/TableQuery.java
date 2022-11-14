package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class TableFilter extends Filter {
    private final JqlSchema schema;

    private final JqlEntityJoin join;

    protected TableFilter(TableFilter parentQuery, JqlSchema schema) {
        this(parentQuery, schema, null, true);
    }

    public TableFilter(TableFilter parentQuery, JqlSchema schema, JqlEntityJoin join, boolean fetchData) {
        super(parentQuery);
        this.schema = schema;
        this.join = join;
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public TableFilter asTableFilter() {
        return this;
    }

    public String getTableName() { return schema.getTableName(); }

    public TableFilter getTableFilter() {
        return this;
    }

    @Override
    public Filter getQueryScope_impl(String key, Type isLeaf_unused, boolean fetchData) {
        JqlEntityJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != JsonNodeType.Object) return this;
        }

        Filter subQuery = subQueries.get(key);
        if (subQuery == null) {
            if (join != null) {
                JqlSchema subSchema = getTopQuery().addTableJoin(join, fetchData);
                subQuery = new TableFilter(this, subSchema, join, fetchData);
            }
            else {
                subQuery = new JsonFilter(this, key);
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
        for (Filter q : subQueries.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                jqlEntityJoinVisitor.visitJoinedSchema(table);
            }
        }
    }
}
