package org.slowcoders.jql.parser;

import java.util.HashMap;

abstract class Filter extends PredicateSet {

    protected HashMap<String, Filter> subEntities = new HashMap<>();

    public Filter(Conjunction delimiter) {
        super(delimiter);
    }

    public JsonFilter asJsonFilter() { return null; }

    public EntityFilter asEntityFilter() { return null; }

    public abstract EntityFilter getTable();

    public Filter getContainingFilter(JqlQuery query, String key, boolean isLeaf) {
        Filter entity = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            EntityFilter table = entity.asEntityFilter();
            if (table != null && table.getSchema().hasColumn(key)) {
                return entity;
            }
            String token = key.substring(0, p);
            entity = entity.getContainingFilter_impl(query, token, false);
            key = key.substring(p + 1);
        }
        return entity.getContainingFilter_impl(query, key, isLeaf);
    }

    protected abstract Filter getContainingFilter_impl(JqlQuery query, String key, boolean isLeaf);

    public abstract void writeAttribute(QueryBuilder sb, String key, Class<?> valueType);

    public abstract Filter createFilter(Conjunction conjunction);

    public abstract String getColumnName(String key);

}
