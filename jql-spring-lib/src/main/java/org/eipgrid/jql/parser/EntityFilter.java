package org.eipgrid.jql.parser;

import org.eipgrid.jql.schema.JQSchema;

import java.util.HashMap;

public abstract class EntityFilter {

    private final EntityFilter parent;
    private final PredicateSet predicates;
    protected final HashMap<String, EntityFilter> subFilters = new HashMap<>();

    protected EntityFilter(EntityFilter parentNode) {
        this.predicates = new PredicateSet(Conjunction.AND, this);
        this.parent = parentNode;
    }

    public abstract String getMappingAlias();

    public final boolean isJsonNode() { return getSchema() == null; }

    /** return null if schemaless node. cf) json node */
    public JQSchema getSchema() { return null; }

    public boolean isEmpty() { return predicates.isEmpty(); }

    public Expression getPredicates() {
        return predicates;
    }

    final PredicateSet getPredicateSet() {
        return predicates;
    }

    public EntityFilter getParentNode() { return this.parent; }

    public JqlQuery getRootNode() {
        return parent.getRootNode();
    }

    protected abstract EntityFilter makeSubNode(String key, JqlParser.NodeType nodeType);

    protected abstract String getColumnName(String key);

    TableFilter asTableFilter() { return null; }

    void setSelectedProperties(String[] selectedKeys) {
    }


    final EntityFilter getFilterNode(String key, JqlParser.NodeType nodeType) {
        if (key == null) return this;

        EntityFilter scope = this;
        for (int p; (p = key.indexOf('.')) > 0; ) {
            JQSchema schema = scope.getSchema();
            if (schema != null && schema.hasColumn(key)) {
                // TODO 고려 사항
                //  1) '.' 으로 이어진 Composite Key 는 Leaf-Column(Not joined) 에만 사용 가능.
                //  2) '.' 으로 이어진 Composite Key 키에 대해서도 Join-Column 허용?
                return scope;
            }
            String token = key.substring(0, p);
            scope = scope.makeSubNode(token, nodeType);
            key = key.substring(p + 1);
        }
        scope = scope.makeSubNode(key, nodeType);
        return scope;
    }

    public Iterable<EntityFilter> getChildNodes() {
        return subFilters.values();
    }

    protected void addComparedPropertyToSelection(String key) {}

    protected void setSelectedProperties_withEmptyFilter() {
    }
}
