package org.eipgrid.jql.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public abstract class QColumn {
    private final QSchema schema;
    private final String storedName;
    private Class storedType;
    private boolean isReference;

    protected QColumn(QSchema schema, String storedName, Class storedType) {
        this.schema = schema;
        this.storedName = storedName;
        this.storedType = storedType;
        this.isReference = Object.class == storedType ||
                JsonNode.class.isAssignableFrom(storedType) ||
                Collection.class.isAssignableFrom(storedType);
    }

//    protected QColumn(QSchema schema, String storedName, Class javaType) {
//        this(schema, storedName, QType.of(javaType));
//    }

    public final QSchema getSchema() {
        return schema;
    }

    public final Class getStoredType() {
        return storedType;
    }

    public final String getStoredName() {
        return storedName;
    }

    public boolean isReference() {
        return this.isReference;
    }

    public boolean isJsonNode() {
        return this.storedType == JsonNode.class;
    }

    public abstract String getJsonKey();

    public Field getMappedOrmField() { return null; }

    protected void setMappedOrmField(Field f) {}

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
        return storedName.hashCode();
    }

    public String toString() { return getSchema().getSimpleTableName() + "::" + this.getJsonKey()+ "<" + storedName + ">"; }

}
