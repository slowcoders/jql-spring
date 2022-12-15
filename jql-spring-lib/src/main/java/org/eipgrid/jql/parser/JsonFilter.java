package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSchema;

import java.util.HashMap;

class JsonFilter extends JqlNode {
    private final String key;

    JsonFilter(JqlNode parentQuery, String key) {
        super(parentQuery);
        this.key = key;
    }

    @Override
    public String getMappingAlias() {
        return this.key;
    }

    @Override
    public JqlNode makeSubNode(String key, ValueNodeType nodeType) {
        if (nodeType == ValueNodeType.Leaf) {
            return this;
        }
        JqlNode entity = subFilters.get(key);
        if (entity == null) {
            entity = new JsonFilter(this, key);
            subFilters.put(key, entity);
        }
        return entity;
    }


    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }


}
