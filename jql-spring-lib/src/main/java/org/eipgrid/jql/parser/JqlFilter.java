package org.eipgrid.jql.parser;

import org.eipgrid.jql.JQSchema;

import java.util.HashMap;

public abstract class JqlFilter implements Expression {

    private final JqlFilter parent;
    private final PredicateSet predicates;
    protected final HashMap<String, JqlFilter> subFilters = new HashMap<>();

    protected JqlFilter(JqlFilter parentNode) {
        this.predicates = new PredicateSet(Conjunction.AND, this);
        this.parent = parentNode;
    }

    public abstract String getMappingAlias();

    public final boolean isJsonNode() { return getSchema() == null; }

    /** return null if schemaless node. cf) json node */
    public JQSchema getSchema() { return null; }

    public void accept(AstVisitor visitor) {
        visitor.visitNode(this);
    }

    public boolean isEmpty() { return predicates.isEmpty(); }

    public Expression getPredicates() {
        return predicates;
    }

    final PredicateSet getPredicateSet() {
        return predicates;
    }

    public JqlFilter getParentNode() { return this.parent; }

    public JqlQuery getRootNode() {
        return parent.getRootNode();
    }

    protected abstract JqlFilter makeSubNode(String key, JqlNodeType nodeType);

    protected abstract String getColumnName(String key);

    TableFilter asTableFilter() { return null; }

    void setSelectedProperties(String[] selectedKeys) {
    }

    KeySet getDefaultJoinedPropertySelection() {
        return KeySet.All;
    }

    final JqlFilter getFilterNode(String key, JqlNodeType nodeType) {
        if (key == null) return this;

        JqlFilter scope = this;
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

    public Iterable<JqlFilter> getChildNodes() {
        return subFilters.values();
    }

    protected void addComparedAttribute(String key) {}
}
