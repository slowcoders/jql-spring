package org.eipgrid.jql.json;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.jdbc.metadata.JdbcColumn;
import org.eipgrid.jql.util.ClassUtils;

import java.util.HashMap;

public class JsonJql {

    public static String createDDL(JqlSchema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("const " + schema.getTableName() + "Schema = [\n");
        for (JqlColumn col : schema.getReadableColumns()) {
            dumpJSONSchema(sb, (JdbcColumn)col);
            sb.append(",\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    public static String dumpJSONSchema(StringBuilder sb, JdbcColumn col) {
        String jsonType = getColumnType(col.getJavaType());
        if (jsonType == null) {
            throw new RuntimeException("JsonType not registered: " + col.getJavaType() + " " + col.getColumnName());
        }

//        String max = getPrecision(col, jsonType);
        sb.append("  ").append(col.getJsonKey());
        while (sb.length() < 20) {
            sb.append(' ');
        }
        sb.append(" : jql.").append(jsonType).append("('")
                .append(col.getLabel()).append("'");
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

            sb.append(schema.getTableName()).append("Schema.join(\"");
            sb.append(jqlColumn.getColumnName()).append("\", ");
            sb.append(joined_pk.getSchema().getEntityClassName()).append("Schema, \"");
            sb.append(joined_pk.getJsonKey()).append("\", \"");
            sb.append(jqlColumn.getJsonKey()).append("\");\n");
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

        /**
         * https://www.postgresql.org/docs/current/datatype.html
         */
        mdkTypes.put("json", "Object");
        mdkTypes.put("jsonb", "Object");
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
}
