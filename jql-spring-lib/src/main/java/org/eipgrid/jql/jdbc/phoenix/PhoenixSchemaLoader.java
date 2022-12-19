package org.eipgrid.jql.jdbc.phoenix;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlEntityJoin;
import org.eipgrid.jql.SchemaLoader;
import org.eipgrid.jql.jdbc.metadata.JdbcColumn;
import org.eipgrid.jql.jdbc.metadata.JdbcSchema;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.validation.constraints.Max;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoenixSchemaLoader extends SchemaLoader {
    private static HashMap<Class<?>, String> typeMap = new HashMap<>();

    public PhoenixSchemaLoader() {
        super(AttributeNameConverter.defaultConverter);
    }


    @Override
    public JdbcSchema loadSchema(String tablePath) {
        throw new RuntimeException("not implemented");
    }

    public String createDDL(JqlSchema schema) {
        String tableName = schema.getTableName();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(\n");

        List<JqlColumn> pks = schema.getPKColumns();

        for (JqlColumn col : schema.getReadableColumns()) {
            sb.append(col.getColumnName()).append(" ").append(((JdbcColumn)col).getDBColumnType());
            if (pks.contains(col)) {
                sb.append(" NOT NULL");
            }
            sb.append(",\n");
        }
        sb.setLength(sb.length() - 2);
        sb.append("\nCONSTRAINT PK PRIMARY KEY(");
        for (JqlColumn col : pks) {
            sb.append(col.getColumnName());
            if (col.getJavaType() == Timestamp.class) {
                sb.append(" ROW_TIMESTAMP");
            }
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("))");//\nSALT_BUCKETS = 8");
        return sb.toString();
    }

    @Override
    public String toColumnType(Class<?> javaType, Field f) {
        Class<?> type = f.getType();
        Max max = f.getAnnotation(Max.class);

        String colType;
        if (type.isArray()) {
            String elemType = typeMap.get(type.getComponentType());
            String dim = max == null ? "[]" : "[" + max + "]";
            colType = elemType + " ARRAY" + dim;
        }
        else {
            colType = typeMap.get(type);
            if (colType == null) {
                throw new RuntimeException("unknown type: " + type);
            }
            if (colType.getClass() == String.class && max != null) {
                colType += "(" + max + ")";
            }
        }
        return colType;
    }

    @Override
    protected HashMap<String, JqlEntityJoin> loadJoinMap(JqlSchema jqlSchema) {
        return (HashMap<String, JqlEntityJoin>)Collections.EMPTY_MAP;
    }

    static {
        typeMap.put(boolean.class, "TINYINT");
        typeMap.put(byte.class, "TINYINT");
        typeMap.put(char.class, "UNSIGNED_SMALLINT");
        typeMap.put(short.class, "SMALLINT");
        typeMap.put(int.class, "INT");
        typeMap.put(long.class, "BIGINT");
        typeMap.put(float.class, "FLOAT");
        typeMap.put(double.class, "DOUBLE");

        typeMap.put(Boolean.class, "TINYINT");
        typeMap.put(Byte.class, "TINYINT");
        typeMap.put(Character.class, "UNSIGNED_SMALLINT");
        typeMap.put(Short.class, "SMALLINT");
        typeMap.put(Integer.class, "INT");
        typeMap.put(Long.class, "BIGINT");
        typeMap.put(Float.class, "FLOAT");
        typeMap.put(Double.class, "DOUBLE");

        typeMap.put(Date.class, "DATE");
        typeMap.put(Time.class, "TIME");
        typeMap.put(Timestamp.class, "TIMESTAMP");
        typeMap.put(Instant.class, "TIMESTAMP");
        typeMap.put(String.class, "VARCHAR");

        // json 처리.
        typeMap.put(Object.class, "VARCHAR");
        typeMap.put(Map.class, "VARCHAR");
    }

}
