package org.slowcoders.jql;

import org.slowcoders.jql.util.AttributeNameConverter;
import org.slowcoders.jql.util.ClassUtils;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.lang.reflect.Field;

public abstract class JqlColumn {
    private final String columnName;
    private final Class<?> javaType;
    private final JsonNodeType valueFormat;

    private final JqlSchema schema;

    protected JqlColumn(JqlSchema schema, String columnName, Class<?> javaType, JsonNodeType valueFormat) {
        this.schema = schema;
        this.javaType = javaType;
        this.columnName = columnName;
        this.valueFormat = valueFormat;
    }

    protected JqlColumn(JqlSchema schema, Class javaType, String columnName) {
        this(schema, columnName, javaType, JsonNodeType.getNodeType(javaType));
    }

    public JqlColumn(JqlSchema schema, Field f) {
        this.schema = schema;
        this.columnName = resolveColumnName(schema, f);
        this.valueFormat = JsonNodeType.getNodeType(f);
        this.javaType = valueFormat == JsonNodeType.Array ? ClassUtils.getElementType(f) : f.getType();
    }

    public final JqlSchema getSchema() {
        return schema;
    }

    public final Class<?> getJavaType() {
        return javaType;
    }

    public abstract String getJsonName();

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

    private static String resolveColumnName(JqlSchema schema, Field f) {
        if (true) {
            Column c = f.getAnnotation(Column.class);
            if (c != null) {
                String colName = c.name();
                if (colName != null && colName.length() > 0) {
                    return colName;
                }
            }
        }
        if (true) {
            JoinColumn c = f.getAnnotation(JoinColumn.class);
            if (c != null) {
                String colName = c.name();
                if (colName != null && colName.length() > 0) {
                    return colName;
                }
            }
        }
        AttributeNameConverter cvt = schema.getSchemaLoader().getNameConverter();
        String colName = cvt.toPhysicalColumnName(f.getName());
        return colName;
    }
}
