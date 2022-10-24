package org.slowcoders.jql.parser;

import java.util.HashMap;

abstract class QNode extends PredicateSet {

    protected HashMap<String, QNode> subEntities = new HashMap<>();

    public QNode(Conjunction delimiter) {
        super(delimiter);
    }

    public JsonNode asJsonFilter() { return null; }

    public TableNode asEntityFilter() { return null; }

    public abstract TableNode getTable();

    public QNode getContainingFilter(JqlQuery query, String key, boolean isLeaf, boolean fetchData) {
        QNode entity = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            TableNode table = entity.asEntityFilter();
            if (table != null && table.getSchema().hasColumn(key)) {
                // TODO 오류 수정. '.' 으로 이어진 복합키가 Entity 가 아닌 경우만 유효.
                return entity;
            }
            String token = key.substring(0, p);
            entity = entity.getContainingFilter_impl(query, token, false, fetchData);
            key = key.substring(p + 1);
        }
        return entity.getContainingFilter_impl(query, key, isLeaf, fetchData);
    }

    protected abstract QNode getContainingFilter_impl(JqlQuery query, String key, boolean isLeaf, boolean fetchData);

    public abstract void writeAttribute(QueryBuilder sb, String key, Class<?> valueType);

    public abstract QNode createFilter(Conjunction conjunction);

    public abstract String getColumnName(String key);

}
