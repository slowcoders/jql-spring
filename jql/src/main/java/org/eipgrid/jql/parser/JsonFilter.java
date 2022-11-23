package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSchema;

class JsonFilter extends Filter implements JqlFilterNode {
    private final String key;

    JsonFilter(Filter parentQuery, String key) {
        super(parentQuery);
        this.key = key;
    }

    public JqlSchema getSchema() {
        return getTableFilter().getSchema();
    }

    public JsonFilter asJsonFilter() {
        return this;
    }

    public boolean isJsonNode() {
        return true;
    }

    @Override
    public String getMappingAlias() {
        return this.key;
    }

    @Override
    public Filter getFilter_impl(String key, ValueNodeType nodeType, boolean fetchData_unused) {
        if (nodeType == ValueNodeType.Leaf) {
            return this;
        }
        Filter entity = subFilters.get(key);
        if (entity == null) {
            entity = new JsonFilter(this, key);
            subFilters.put(key, entity);
        }
        return entity;
    }

    @Override
    public void accept(JqlPredicateVisitor visitor) {
        JqlFilterNode old = visitor.setCurrentNode(this);
        super.accept(visitor);
        visitor.setCurrentNode(old);
    }

    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }


}
