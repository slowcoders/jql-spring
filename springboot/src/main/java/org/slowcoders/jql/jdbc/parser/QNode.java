package org.slowcoders.jql.jdbc.parser;

import java.util.HashMap;

abstract class QNode extends QuerySet {

    protected HashMap<String, QNode> subEntities = new HashMap<>();

    public QNode(Type delimiter) {
        super(delimiter);
    }

    public QJsonNode asJsonNode() { return null; }

    public QTableNode asTableNode() { return null; }

    public abstract QTableNode getTable();

    public QNode getContainingEntity(JqlQuery query, String key, boolean isLeaf) {
        QNode entity = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            QTableNode table = entity.asTableNode();
            if (table != null && table.getSchema().hasColumn(key)) {
                return entity;
            }
            String token = key.substring(0, p);
            entity = entity.getContainingEntity_impl(query, token, false);
            key = key.substring(p + 1);
        }
        return entity.getContainingEntity_impl(query, key, isLeaf);
    }

    protected abstract QNode getContainingEntity_impl(JqlQuery query, String key, boolean isLeaf);

    public abstract void writeAttribute(SQLWriter sb, String key, Class<?> valueType);

    public abstract QNode createQuerySet(Type type);

    public abstract String getColumnName(String key);

}
