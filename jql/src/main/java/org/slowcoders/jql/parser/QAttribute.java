package org.slowcoders.jql.parser;

class QAttribute {
    private final QNode scope;
    private final String key;
    private final Class<?> valueType;

    QAttribute(QNode scope, String key, Class<?> valueType) {
        this.scope = scope;
        this.key = key;
        this.valueType = valueType;
    }

    public void printSQL(QueryBuilder sb) {
        scope.writeAttribute(sb, key, valueType);
    }
}
