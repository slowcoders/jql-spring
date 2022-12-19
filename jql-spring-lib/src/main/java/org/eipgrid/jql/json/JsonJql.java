package org.eipgrid.jql.json;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlEntityJoin;
import org.eipgrid.jql.jdbc.metadata.JdbcColumn;
import org.eipgrid.jql.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class JsonJql {

    public static String createDDL(JqlSchema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("const " + schema.getSimpleTableName() + "Schema_columns = [\n");
        for (JqlColumn col : schema.getReadableColumns()) {
            dumpJSONSchema(sb, (JdbcColumn)col);
            sb.append(",\n");
        }
        sb.append("];\n");

        if (!schema.getEntityJoinMap().isEmpty()) {
            sb.append("\nconst " + schema.getSimpleTableName() + "Schema_external_entities = [\n");
            for (Map.Entry<String, JqlEntityJoin> entry : schema.getEntityJoinMap().entrySet()) {
                sb.append("  jql.externalJoin(\"").append(entry.getKey()).append("\", ");
                sb.append(entry.getValue().getJoinedSchema().getSimpleTableName()).append("Schema, \n");
                sb.append(entry.getValue().isUniqueJoin() ? "{}" : "[]").append("),\n");
            }
            sb.append("];\n");
        }

        return sb.toString();
    }

    public static String dumpJSONSchema(StringBuilder sb, JdbcColumn col) {
        String jsonType = getColumnType(col.getJavaType());
        if (jsonType == null) {
            throw new RuntimeException("JsonType not registered: " + col.getJavaType() + " " + col.getColumnName());
        }

////        String max = getPrecision(col, jsonType);
//        sb.append("  \"").append(col.getJsonKey()).append('"');
//        while (sb.length() < 20) {
//            sb.append(' ');
//        }
        sb.append("  jql.").append(jsonType).append("(\"")
                .append(col.getJsonKey()).append("\"");
//        if (max != null) {
//            sb.append(", ").append(max);
//        }
        sb.append(")");

        if (col.isReadOnly()) {
            sb.append(".readOnly()");
        }
        else if (!col.isNullable()) {
            sb.append(".required()");
        }

        return sb.toString();
    }



    public static String createJoinJQL(JqlSchema schema) {
        StringBuilder sb = new StringBuilder();
        for (JqlColumn jqlColumn : schema.getReadableColumns()) {
            JqlColumn joined_pk = jqlColumn.getJoinedPrimaryColumn();
            if (joined_pk == null) continue;

            sb.append(schema.getSimpleTableName()).append("Schema.join(\"");
            sb.append(jqlColumn.getJsonKey()).append("\", ");
            sb.append(joined_pk.getSchema().getEntityClassName()).append("Schema, \"");
            sb.append(joined_pk.getJsonKey()).append("\");\n");
        }
        return sb.toString();
    }

    private static String getJoinName(String name) {
        int i = name.lastIndexOf('_');
        while (i > 0) {
            if (name.charAt(--i) != '_') {
                name = name.substring(0, i+1);
                break;
            }
        }
        return name;
    }

    public static String getColumnType(Class<?> javaType) {
        String type = ClassUtils.getBoxedType(javaType).getName();
        String colType = mdkTypes.get(type);
        if (colType == null) {
            colType = "Object";
            //throw new RuntimeException(type + " is not registered");
        }
        return colType;
    }

    private static final HashMap<String, String> mdkTypes = new HashMap<>();
    static {
        mdkTypes.put("java.lang.String", "Text");
        mdkTypes.put("java.lang.Character", "Text");

        mdkTypes.put("java.lang.Boolean", "Boolean");

        mdkTypes.put("java.lang.Byte", "Number");
        mdkTypes.put("java.lang.Short", "Number");
        mdkTypes.put("java.lang.Integer", "Number");
        mdkTypes.put("java.lang.Long", "Number");
        mdkTypes.put("java.lang.Float", "Number");
        mdkTypes.put("java.lang.Double", "Number");
        mdkTypes.put("java.math.BigDecimal", "Number");

        /*
         date 형은 max(13) : 날짜만.
         time 형은 max(15) : 시간만.
         timestamp 형은 max(29)
         timestamptz 형은 max(35)
         */
        mdkTypes.put("java.sql.Date", "Date");
        mdkTypes.put("java.sql.Time", "Time");
        mdkTypes.put("java.sql.Timestamp", "Timestamp");
        mdkTypes.put("org.postgresql.util.PGInterval", "TimeInterval");

        mdkTypes.put("java.util.Map", "Object");
        mdkTypes.put("java.util.Map", "Object");
        mdkTypes.put("Array", "Array");

        /**
         * https://www.postgresql.org/docs/current/datatype.html
         */
        mdkTypes.put("json", "EmbeddedObject");
        mdkTypes.put("jsonb", "EmbeddedObject");
        mdkTypes.put("Array", "Array");

        if (false) {
            mdkTypes.put("UUID", "UUID");
            mdkTypes.put("hstore", "Text");
            mdkTypes.put("box", "Box");
            mdkTypes.put("lseg", "Array");
            mdkTypes.put("point", "Point");
            mdkTypes.put("polygon", "Polygon");
            mdkTypes.put("inet", "Inet4"); // IP4 Address
            mdkTypes.put("macaddr", "Text"); // mac Address
        }
    }

    static String filler = "            ";
    public static String getSimpleSchema(JqlSchema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type").append(filler.substring("Type".length())).append("Key(physical_column_name)\n");
        sb.append("--------------------------------------------------\n");
        for (JqlColumn col : schema.getReadableColumns()) {
            String columnType = getColumnType(col.getJavaType());
            if (!col.isNullable()) {
                columnType = columnType + '!';
            }
            sb.append(columnType).append(filler.substring(columnType.length()));
            sb.append(col.getJsonKey()).append('(').append(col.getColumnName()).append(')');
            JqlColumn joinedPK;
            if (col.isPrimaryKey()) {
                sb.append(" PK");
            }
            else if ((joinedPK = col.getJoinedPrimaryColumn()) != null) {
                sb.append(" FK -> ");
                sb.append(joinedPK.getSchema().getTableName());
                sb.append(".").append(joinedPK.getJsonKey());
            }
            sb.append("\n");
        }

        if (!schema.getEntityJoinMap().isEmpty()) {
            HashMap<String, JqlEntityJoin> associativeColumns = new HashMap<>();
            sb.append("\n// external entities //\n");
            for (Map.Entry<String, JqlEntityJoin> entry : schema.getEntityJoinMap().entrySet()) {
                JqlEntityJoin join = entry.getValue();
                if (join.getAssociativeJoin() != null) {
                    associativeColumns.put(entry.getKey(), join);
                    continue;
                }
                sb.append(entry.getKey());
                if (!join.isUniqueJoin()) sb.append("[]");
                sb.append(" -> ");
                sb.append(join.getJoinedSchema().getSimpleTableName());
                sb.append("\n");
            }

            sb.append("\n// associative entities //\n");
            for (Map.Entry<String, JqlEntityJoin> entry : associativeColumns.entrySet()) {
                JqlEntityJoin join = entry.getValue();
                sb.append(entry.getKey());
                if (!join.isUniqueJoin()) sb.append("[]");
                sb.append(" -> ");
                sb.append(join.getJoinedSchema().getSimpleTableName()).append(".");
                sb.append(join.getAssociatedSchema().getSimpleTableName());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
