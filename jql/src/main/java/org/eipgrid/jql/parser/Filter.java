package org.eipgrid.jql.parser;

import java.util.HashMap;

abstract class Filter extends PredicateSet implements JqlFilterNode {

    private final Filter parent;
    final HashMap<String, Filter> subFilters = new HashMap<>();

    public Filter(Filter parentQuery) {
        super(Conjunction.AND);
        this.parent = parentQuery;
    }

    public JsonFilter asJsonFilter() { return null; }

    public TableFilter asTableFilter() { return null; }

    public Filter getParentNode() { return this.parent; }

    Filter getBaseFilter() { return this; }


    public TableFilter getTableFilter() {
        return parent.getTableFilter();
    }

    public JqlQuery getRootFilter() {
        return parent.getRootFilter();
    }

    public void setSelectedColumns(String[] jsonKeys) {}

    public Filter getFilterNode(String key, ValueNodeType nodeType) {
        if (key == null) return this;

        Filter scope = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            TableFilter table = scope.asTableFilter();
            if (table != null && table.getSchema().hasColumn(key)) {
                // TODO 고려 사항
                //  1) '.' 으로 이어진 Composite Key 는 Leaf-Column(Not joined) 에만 사용 가능.
                //  2) '.' 으로 이어진 Composite Key 키에 대해서도 Join-Column 허용?
                return scope;
            }
            String token = key.substring(0, p);
            scope = scope.getFilter_impl(token, nodeType);
            key = key.substring(p + 1);
        }
        scope = scope.getFilter_impl(key, nodeType);
        return scope;
    }

    protected abstract Filter getFilter_impl(String key, ValueNodeType nodeType);

    public abstract String getColumnName(String key);

}
