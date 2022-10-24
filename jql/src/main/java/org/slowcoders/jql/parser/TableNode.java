package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class TableNode extends QNode {
    private final JqlSchema schema;

    public TableNode(JqlSchema schema) {
        this(schema, Conjunction.AND);
    }

    public TableNode(JqlSchema schema, Conjunction delimiter) {
        super(delimiter);
        this.schema = schema;
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public TableNode asEntityFilter() {
        return this;
    }

    public String getTableName() { return schema.getTableName(); }

    public TableNode getTable() {
        return this;
    }

    @Override
    public QNode getContainingFilter_impl(JqlQuery query, String key, boolean isLeaf_unused, boolean fetchData) {
        JqlEntityJoin joinKeys = schema.getJoinedColumnSet(key);
        if (joinKeys == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != JsonNodeType.Object) return this;
        }

        QNode entity = subEntities.get(key);
        if (entity == null) {
            if (joinKeys != null) {
                entity = query.addTableJoin(joinKeys, fetchData);
            }
            else {
                entity = new JsonNode(this, key);
            }
            subEntities.put(key, entity);
            super.add(entity);
        }
        return entity;
    }

    @Override
    public void writeAttribute(QueryBuilder sb, String key, Class<?> valueType) {
        sb.write(getSchema().getTableName()).write(".").write(key);
    }

    @Override
    public QNode createFilter(Conjunction conjunction) {
        return new TableNode(this.schema, conjunction);
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
