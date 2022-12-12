package org.eipgrid.jql;

public abstract class JqlColumn {
    private final String columnName;
    private final Class<?> javaType;
    private final JsonNodeType valueFormat;

    private final JqlSchema schema;
    private String javaFieldName;

    protected JqlColumn(JqlSchema schema, String columnName, Class<?> javaType, JsonNodeType valueFormat) {
        this.schema = schema;
        this.javaType = javaType;
        this.columnName = columnName;
        this.valueFormat = valueFormat;
    }

    protected JqlColumn(JqlSchema schema, String columnName, Class javaType) {
        this(schema, columnName, javaType, JsonNodeType.getNodeType(javaType));
    }

    public final JqlSchema getSchema() {
        return schema;
    }

    public final Class<?> getJavaType() {
        return javaType;
    }

    public abstract String getJsonKey();

    public final String getJavaFieldName() {
        if (javaFieldName == null) {
            String qName = getJsonKey();
            int idx = qName.indexOf('.');
            if (idx > 0) {
                qName = qName.substring(0, idx);
            }
            javaFieldName = qName;
        }
        return javaFieldName;
    }

    public final String getColumnName() {
        return columnName;
    }

    public final JsonNodeType getValueFormat() {
        return valueFormat;
    }

    //===========================================================
    // Overridable Properties
    //-----------------------------------------------------------

    public boolean isReadOnly() {
        return false;
    }

    public boolean isNullable() {
        return true;
    }

    public boolean isAutoIncrement() {
        return false;
    }

    public boolean isPrimaryKey() { return false; }

    public String getLabel() {
        return null;
    }

    public JqlColumn getJoinedPrimaryColumn() {
        return null;
    }

    public String getDBColumnType() {
        return null;
    }

}
