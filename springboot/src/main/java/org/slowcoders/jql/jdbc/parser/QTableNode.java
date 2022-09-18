package org.slowcoders.jql.jdbc.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.ValueFormat;

class QTableNode extends QNode {
    private final JqlSchema schema;
    private boolean fetchData;

    public QTableNode(JqlSchema schema) {
        this(schema, Type.AND);
    }

    public QTableNode(JqlSchema schema, Type delimiter) {
        super(delimiter);
        this.schema = schema;
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public QTableNode asTableNode() {
        return this;
    }

    public String getTableName() { return schema.getTableName(); }

    public QTableNode getTable() {
        return this;
    }

    @Override
    public QNode getContainingEntity_impl(JqlQuery query, String key, boolean valueType2) {
        JqlSchemaJoin join = schema.getJoinedForeignKeys(key);
        if (join == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != ValueFormat.Embedded) return this;
        }

        QNode entity = subEntities.get(key);
        if (entity == null) {
            if (join != null) {
                entity = new QTableNode(join.getJoinedTable());
                query.addTableJoin(join);
            }
            else {
                entity = new QJsonNode(this, key);
            }
            subEntities.put(key, entity);
            super.add(entity);
        }
        return entity;
    }

    @Override
    public void writeAttribute(SQLWriter sb, String key, Class<?> valueType) {
        sb.write(getSchema().getTableName()).write(".").write(key);
    }

    @Override
    public QNode createQuerySet(Type type) {
        return new QTableNode(this.schema, type);
    }

    @Override
    public void printSQL(SQLWriter sb) {
        JqlSchema old = sb.pushJoinedTable(this.schema);
        super.printSQL(sb);
        sb.replaceTableInfo(old);
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

    public void setFetchData(boolean needDataFetch, JqlQuery query) {
        if (needDataFetch && !this.fetchData) {
            query.markFetchData(this.getSchema());
        }
        this.fetchData |= needDataFetch;
    }

    public boolean getFetchData() {
        return fetchData;
    }
}
