package org.slowcoders.jql.parser;

import java.util.HashMap;

abstract class QueryNode extends PredicateSet {

    protected HashMap<String, QueryNode> subEntities = new HashMap<>();

    public QueryNode(Conjunction delimiter) {
        super(delimiter);
    }

    public JsonQuery asJsonNode() { return null; }

    public TableQuery asTableNode() { return null; }

    public abstract TableQuery getTable();

    public QueryNode getContainingEntity(JqlQuery query, String key, boolean isLeaf) {
        QueryNode entity = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            TableQuery table = entity.asTableNode();
            if (table != null && table.getSchema().hasColumn(key)) {
                return entity;
            }
            String token = key.substring(0, p);
            entity = entity.getContainingEntity_impl(query, token, false);
            key = key.substring(p + 1);
        }
        return entity.getContainingEntity_impl(query, key, isLeaf);
    }

    protected abstract QueryNode getContainingEntity_impl(JqlQuery query, String key, boolean isLeaf);

    public abstract void writeAttribute(SQLWriter sb, String key, Class<?> valueType);

    public abstract QueryNode createQuerySet(Conjunction conjunction);

    public abstract String getColumnName(String key);

}
