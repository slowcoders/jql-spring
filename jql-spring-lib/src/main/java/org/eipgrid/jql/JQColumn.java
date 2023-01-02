package org.eipgrid.jql;

import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;

public abstract class JQColumn {
    private final JQSchema schema;
    private final String columnName;
    private JQType columnType;
    private Class<?> javaType;

    protected JQColumn(JQSchema schema, String columnName, Class<?> javaType, JQType valueFormat) {
        this.schema = schema;
        this.columnName = columnName;
        this.columnType = valueFormat;
        this.javaType = javaType;
    }

    protected JQColumn(JQSchema schema, String columnName, Class javaType) {
        this(schema, columnName, javaType, JQType.of(javaType));
    }

    public final JQSchema getSchema() {
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

    public final JQType getColumnType() {
        return columnType;
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

    public JQColumn getJoinedPrimaryColumn() {
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
        this.columnType = JQType.of(f);
        this.javaType = ClassUtils.getElementType(f);
    }
}
