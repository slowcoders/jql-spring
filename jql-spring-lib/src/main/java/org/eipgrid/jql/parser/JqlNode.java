package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSelect;

public abstract class JqlNode implements Expression {

    private final JqlNode parent;
    private final PredicateSet predicates;

    protected JqlNode(JqlNode parentNode) {
        this.predicates = new PredicateSet(Conjunction.AND, this);
        this.parent = parentNode;
    }

    public abstract String getMappingAlias();

    public final boolean isJsonNode() { return getSchema() == null; }

    /** return null if schemaless node. cf) json node */
    public JqlSchema getSchema() { return null; }

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

    public JqlNode getParentNode() { return this.parent; }

    public AstRoot getRootNode() {
        return parent.getRootNode();
    }

    protected abstract JqlNode makeSubNode(String key, ValueNodeType nodeType);

    protected abstract String getColumnName(String key);

    TableFilter asTableFilter() { return null; }

    void setSelectedColumns(JqlSelect select) {}

    final JqlNode getFilterNode(String key, ValueNodeType nodeType) {
        if (key == null) return this;

        JqlNode scope = this;
        for (int p; (p = key.indexOf('.')) > 0; ) {
            JqlSchema schema = scope.getSchema();
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

}
