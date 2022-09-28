package org.slowcoders.jql.parser;

class QAttribute { // implements QNode {
    private final QueryNode scope;
    private final String key;
    private final Class<?> valueType;

    QAttribute(QueryNode scope, String key, Class<?> valueType) {
        this.scope = scope;
        this.key = key;
        this.valueType = valueType;
    }

    public void printSQL(SQLWriter sb) {
        scope.writeAttribute(sb, key, valueType);
    }
}
