package org.slowcoders.jql;

import org.slowcoders.jql.jdbc.timescale.Aggregate;

public abstract class JqlColumn {
    protected String columnName;
    protected String fieldName;
    protected Class<?> javaType;
    protected ValueFormat valueFormat;
    protected Aggregate.Type aggregationType;

    private final JqlSchema schema;

    protected JqlColumn(JqlSchema schema) {
        this.schema = schema;
    }

    protected JqlColumn(JqlSchema schema, Class<?> javaType, String fieldName, String columnName,
                        ValueFormat valueFormat, Aggregate.Type aggregationType) {
        this.schema = schema;
        this.javaType = javaType;
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.valueFormat = valueFormat;
        this.aggregationType = aggregationType;
    }

    public final JqlSchema getSchema() {
        return schema;
    }

    public final Class<?> getJavaType() {
        return javaType;
    }

    public final String getColumnName() {
        return columnName;
    }

    public final String getFieldName() {
        return fieldName;
    }

    public final ValueFormat getValueFormat() {
        return valueFormat;
    }

    public final Aggregate.Type getAggregationType() {
        return this.aggregationType;
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
