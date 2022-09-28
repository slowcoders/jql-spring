package org.slowcoders.jql.parser;

public class QAttribute { // implements QNode {
    private final EntityNode scope;
    private final String key;
    private final Class<?> valueType;

    QAttribute(EntityNode scope, String key, Class<?> valueType) {
        this.scope = scope;
        this.key = key;
        this.valueType = valueType;
    }

    public void printSQL(SQLWriter sb) {
        scope.writeAttribute(sb, key, valueType);
    }
}
