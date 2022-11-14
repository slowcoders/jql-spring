package org.slowcoders.jql.parser;

class QAttribute {
    private final EntityQuery scope;
    private final String key;
    private final Class<?> valueType;

    QAttribute(EntityQuery scope, String key, Class<?> valueType) {
        this.scope = scope;
        this.key = key;
        this.valueType = valueType;
    }

    public void printSQL(SourceWriter sb) {
        scope.writeAttribute(sb, key, valueType);
    }
}
