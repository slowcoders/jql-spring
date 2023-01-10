package org.eipgrid.jql;

import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;

public abstract class JQColumn {
    private final JQSchema schema;
    private final String physicalName;
    private JQType columnType;
    private Class<?> javaType;

    protected JQColumn(JQSchema schema, String physicalName, Class<?> javaType, JQType type) {
        this.schema = schema;
        this.physicalName = physicalName;
        this.columnType = type;
        this.javaType = javaType;
    }

    protected JQColumn(JQSchema schema, String physicalName, Class javaType) {
        this(schema, physicalName, javaType, JQType.of(javaType));
    }

    public final JQSchema getSchema() {
        return schema;
    }

    public final JQType getType() {
        return columnType;
    }

    public final Class<?> getJavaType() {
        return javaType;
    }

    public final String getPhysicalName() {
        return physicalName;
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

    @Override
    public int hashCode() {
        return physicalName.hashCode();
    }

    public String toString() { return getSchema().getSimpleTableName() + "::" + this.getJsonKey()+ "<" + physicalName + ">"; }

    protected void setMappedField(Field f) {
        this.columnType = JQType.of(f);
        this.javaType = ClassUtils.getElementType(f);
    }
}
