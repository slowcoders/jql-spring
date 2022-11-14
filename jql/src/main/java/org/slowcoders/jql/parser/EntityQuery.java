package org.slowcoders.jql.parser;

import java.util.HashMap;

abstract class EntityQuery extends PredicateSet {

    enum Type {
        Leaf,
        Entity,
        Entities
    }

    private final EntityQuery parent;
    final HashMap<String, EntityQuery> subQueries = new HashMap<>();
    private PredicateSet orSet = null;

    public EntityQuery(EntityQuery parentQuery) {
        super(Conjunction.AND);
        this.parent = parentQuery;
    }

    public JsonQuery asJsonQuery() { return null; }

    public TableQuery asTableQuery() { return null; }

    public final EntityQuery getParent() {
        return this.parent;
    }

    EntityQuery getEntityPredicates() { return this; }


    public TableQuery getTableQuery() {
        return parent.getTableQuery();
    }

    public JqlQuery getTopQuery() {
        return parent.getTopQuery();
    }

    public PredicateSet getQueryScope(String key, Type type, boolean fetchData) {
        EntityQuery scope = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            TableQuery table = scope.asTableQuery();
            if (table != null && table.getSchema().hasColumn(key)) {
                // TODO 고려 사항
                //  1) '.' 으로 이어진 Composite Key 는 Leaf-Column(Not joined) 에만 사용 가능.
                //  2) '.' 으로 이어진 Composite Key 키에 대해서도 Join-Column 허용?
                return scope;
            }
            String token = key.substring(0, p);
            scope = scope.getQueryScope_impl(token, type, fetchData);
            key = key.substring(p + 1);
        }
        scope = scope.getQueryScope_impl(key, type, fetchData);
        if (type == Type.Entities) {
            return scope.getOrPredicates();
        } else {
            return scope;
        }
    }

    private PredicateSet getOrPredicates() {
        if (this.orSet == null) {
            this.orSet = new OrPredicates(this);
            this.add(0, this.orSet);
        }
        return this.orSet;
    }

    protected abstract EntityQuery getQueryScope_impl(String key, Type type, boolean fetchData);

    public abstract void writeAttribute(SourceWriter sb, String key, Class<?> valueType);

    public abstract String getColumnName(String key);

    private class OrPredicates extends PredicateSet {
        private final EntityQuery query;

        public OrPredicates(EntityQuery query) {
            super(Conjunction.OR);
            this.query = query;
        }

        @Override
        EntityQuery getEntityPredicates() {
            return query;
        }
    }
}
