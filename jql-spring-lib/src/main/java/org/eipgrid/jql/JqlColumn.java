package org.eipgrid.jql;

import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;

public abstract class JqlColumn {
    private final JqlSchema schema;
    private final String columnName;
    private Class<?> javaType;
    private JqlValueKind valueKind;

    protected JqlColumn(JqlSchema schema, String columnName, Class<?> javaType, JqlValueKind valueFormat) {
        this.schema = schema;
        this.columnName = columnName;
        this.javaType = javaType;
        this.valueKind = valueFormat;
    }

    protected JqlColumn(JqlSchema schema, String columnName, Class javaType) {
        this(schema, columnName, javaType, JqlValueKind.of(javaType));
    }

    public final JqlSchema getSchema() {
        return schema;
    }

    public final Class<?> getJavaType() {
        return javaType;
    }

    public abstract String getJsonKey();

    public String resolveJavaFieldName() {
        String name = getJsonKey();
        int idx = name.indexOf('.');
        if (idx > 0) {
            name = name.substring(0, idx);
        }
        return name;
    }

    public final String getColumnName() {
        return columnName;
    }

    public final JqlValueKind getValueKind() {
        return valueKind;
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

    @Override
    public int hashCode() {
        return columnName.hashCode();
    }

    public String toString() { return getSchema().getSimpleTableName() + "::" + this.getJsonKey()+ "<" + columnName + ">"; }

    protected void setMappedField(Field f) {
        this.valueKind = JqlValueKind.of(f);
        this.javaType = ClassUtils.getElementType(f);
    }
}
