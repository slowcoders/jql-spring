package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

import java.util.List;

class TableQuery extends QueryNode {
    private final JqlSchema schema;
    private boolean fetchData;

    public TableQuery(JqlSchema schema) {
        this(schema, Conjunction.AND);
    }

    public TableQuery(JqlSchema schema, Conjunction delimiter) {
        super(delimiter);
        this.schema = schema;
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public TableQuery asTableNode() {
        return this;
    }

    public String getTableName() { return schema.getTableName(); }

    public TableQuery getTable() {
        return this;
    }

    @Override
    public QueryNode getContainingEntity_impl(JqlQuery query, String key, boolean valueType2) {
        List<JqlColumn> foreignKeys = schema.getJoinedColumnSet(key);
        if (foreignKeys == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != JsonNodeType.Object) return this;
        }

        QueryNode entity = subEntities.get(key);
        if (entity == null) {
            if (foreignKeys != null) {
                entity = query.addTableJoin(foreignKeys);
            }
            else {
                entity = new JsonQuery(this, key);
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
    public QueryNode createQuerySet(Conjunction conjunction) {
        return new TableQuery(this.schema, conjunction);
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
