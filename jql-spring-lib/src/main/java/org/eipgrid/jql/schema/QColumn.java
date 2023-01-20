package org.eipgrid.jql.schema;

import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;

public abstract class QColumn {
    private final QSchema schema;
    private final String physicalName;
    private QType columnType;
    private Class<?> javaType;

    protected QColumn(QSchema schema, String physicalName, Class<?> javaType, QType type) {
        this.schema = schema;
        this.physicalName = physicalName;
        this.columnType = type;
        this.javaType = javaType;
    }

    protected QColumn(QSchema schema, String physicalName, Class javaType) {
        this(schema, physicalName, javaType, QType.of(javaType));
    }

    public final QSchema getSchema() {
        return schema;
    }

    public final QType getType() {
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

    public QColumn getJoinedPrimaryColumn() {
        return null;
    }

    @Override
    public int hashCode() {
        return physicalName.hashCode();
    }

    public String toString() { return getSchema().getSimpleTableName() + "::" + this.getJsonKey()+ "<" + physicalName + ">"; }

    protected void setMappedField(Field f) {
        this.columnType = QType.of(f);
        this.javaType = ClassUtils.getElementType(f);
    }
}
