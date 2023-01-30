package org.eipgrid.jql.schema;

import java.lang.reflect.Field;

public abstract class QColumn {
    private final QSchema schema;
    private final String physicalName;
    private QType columnType;

    protected QColumn(QSchema schema, String physicalName, QType type) {
        this.schema = schema;
        this.physicalName = physicalName;
        this.columnType = type;
    }

    protected QColumn(QSchema schema, String physicalName, Class javaType) {
        this(schema, physicalName, QType.of(javaType));
    }

    public final QSchema getSchema() {
        return schema;
    }

    public final QType getType() {
        return columnType;
    }

    public final Class<?> getJavaType() {
        return columnType.toJavaClass();
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

    public boolean isForeignKey() { return false; }

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

    public Field getMappedOrmField() { return null; }

    protected void setMappedOrmField(Field f) {}
}
