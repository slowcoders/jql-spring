package org.slowcoders.jql.parser;

import java.util.HashMap;

abstract class QScope extends PredicateSet {

    protected HashMap<String, QScope> subEntities = new HashMap<>();

    public QScope(Conjunction delimiter) {
        super(delimiter);
    }

    public JsonScope asJsonScope() { return null; }

    public TableScope asTableScope() { return null; }

    public abstract TableScope getTable();

    public QScope getQueryScope(JqlQuery query, String key, boolean isLeaf, boolean fetchData) {
        QScope entity = this;
        int p;
        while ((p = key.indexOf('.')) > 0) {
            TableScope table = entity.asTableScope();
            if (table != null && table.getSchema().hasColumn(key)) {
                // TODO 고려 사항
                //  1) '.' 으로 이어진 Composite Key 는 Leaf-Column(Not joined) 에만 사용 가능.
                //  2) '.' 으로 이어진 Composite Key 키에 대해서도 Join-Column 허용?
                return entity;
            }
            String token = key.substring(0, p);
            entity = entity.getQueryScope_impl(query, token, false, fetchData);
            key = key.substring(p + 1);
        }
        return entity.getQueryScope_impl(query, key, isLeaf, fetchData);
    }

    protected abstract QScope getQueryScope_impl(JqlQuery query, String key, boolean isLeaf, boolean fetchData);

    public abstract void writeAttribute(QueryBuilder sb, String key, Class<?> valueType);

    public abstract QScope createQueryScope(Conjunction conjunction);

    public abstract String getColumnName(String key);

}
