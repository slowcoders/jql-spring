package org.slowcoders.jql.parser;

import java.util.HashMap;

abstract class EntityNode extends QuerySet {

    protected HashMap<String, EntityNode> subEntities = new HashMap<>();

    public EntityNode(Conjunction delimiter) {
        super(delimiter);
    }

    public JsonNode asJsonNode() { return null; }

    public TableNode asTableNode() { return null; }

    public abstract TableNode getTable();

    public EntityNode getContainingEntity(JqlQuery query, String key, boolean isLeaf) {
        EntityNode entity = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            TableNode table = entity.asTableNode();
            if (table != null && table.getSchema().hasColumn(key)) {
                return entity;
            }
            String token = key.substring(0, p);
            entity = entity.getContainingEntity_impl(query, token, false);
            key = key.substring(p + 1);
        }
        return entity.getContainingEntity_impl(query, key, isLeaf);
    }

    protected abstract EntityNode getContainingEntity_impl(JqlQuery query, String key, boolean isLeaf);

    public abstract void writeAttribute(SQLWriter sb, String key, Class<?> valueType);

    public abstract EntityNode createQuerySet(Conjunction conjunction);

    public abstract String getColumnName(String key);

}
