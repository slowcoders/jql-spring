package org.eipgrid.jql.parser;

class JsonFilter extends JqlFilter {
    private final String key;

    JsonFilter(JqlFilter parentQuery, String key) {
        super(parentQuery);
        this.key = key;
    }

    @Override
    public String getMappingAlias() {
        return this.key;
    }

    @Override
    public JqlFilter makeSubNode(String key, JqlNodeType nodeType) {
        if (nodeType == JqlNodeType.Leaf) {
            return this;
        }
        JqlFilter entity = subFilters.get(key);
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
