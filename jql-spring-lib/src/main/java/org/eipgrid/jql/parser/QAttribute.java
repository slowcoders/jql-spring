package org.eipgrid.jql.parser;

class QAttribute {
    private final Filter scope;
    private final String key;
    private final Class<?> valueType;

    QAttribute(Filter scope, String key, Class<?> valueType) {
        this.scope = scope;
        this.key = key;
        this.valueType = valueType;
    }

    public String getColumnName() {
        return key;
    }

    public void printSQL(SourceWriter sb) {
//        scope.writeAttribute(sb, key, valueType);
    }
}
