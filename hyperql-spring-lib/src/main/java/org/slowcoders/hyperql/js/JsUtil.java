package org.slowcoders.hyperql.js;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;
import org.slowcoders.hyperql.schema.QJoin;
import org.slowcoders.hyperql.jdbc.storage.JdbcColumn;
import org.slowcoders.hyperql.util.CaseConverter;
import org.slowcoders.hyperql.util.ClassUtils;
import org.slowcoders.hyperql.util.SourceWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsUtil {

    public static String createJsonModel(QSchema schema) {
        SourceWriter sb = new SourceWriter('"');
        sb.write("[\n");
        sb.incTab();
        for (QColumn col : schema.getBaseColumns()) {
            dumpJsonSchema(sb, (JdbcColumn)col);
            sb.write(",\n");
        }
        sb.incTab();
        sb.write("];\n");

//        if (!schema.getEntityJoinMap().isEmpty() || !schema.getExtendedColumns().isEmpty()) {
//            sb.write("\nconst " + schema.getSimpleName() + "Schema_external_entities = [\n");
//            for (QColumn column : schema.getExtendedColumns()) {
//                if (column.isForeignKey()) {
//                    sb.write("  hql.externalJoin(\"").write(column.getJsonKey()).write("\", Object,\n");
//                }
//            }
//            for (Map.Entry<String, QJoin> entry : schema.getEntityJoinMap().entrySet()) {
//                sb.write("  hql.externalJoin(\"").write(entry.getKey()).write("\", ");
//                sb.write(entry.getValue().getTargetSchema().getSimpleName()).write("Schema, ");
//                sb.write(entry.getValue().hasUniqueTarget() ? "Object" : "Array").write("),\n");
//            }
//            sb.write("];\n");
//        }

        return sb.toString();
    }

    public static String createJsSchema(QSchema schema) {
        SourceWriter sb = new SourceWriter('"');
        sb.write("const " + schema.getSimpleName() + "Schema_columns = [\n");
        for (QColumn col : schema.getBaseColumns()) {
            dumpJqlSchema(sb, (JdbcColumn)col);
            sb.write(",\n");
        }
        sb.write("];\n");

        if (!schema.getEntityJoinMap().isEmpty() || !schema.getExtendedColumns().isEmpty()) {
            sb.write("\nconst " + schema.getSimpleName() + "Schema_external_entities = [\n");
            for (QColumn column : schema.getExtendedColumns()) {
                if (column.isForeignKey()) {
                    sb.write("  hql.externalJoin(\"").write(column.getJsonKey()).write("\", Object,\n");
                }
            }
            for (Map.Entry<String, QJoin> entry : schema.getEntityJoinMap().entrySet()) {
                sb.write("  hql.externalJoin(\"").write(entry.getKey()).write("\", ");
                sb.write(entry.getValue().getTargetSchema().getSimpleName()).write("Schema, ");
                sb.write(entry.getValue().hasUniqueTarget() ? "Object" : "Array").write("),\n");
            }
            sb.write("];\n");
        }

        return sb.toString();
    }

    static String toFormType(JsType jsType) {
        switch (jsType) {
            case Text: return "textfield";
            case Integer: return "number";
            case Float: return "number";
            case Boolean: return "boolean";
            case Date: return "date";
            case Time: return "time";
            case Timestamp: return "timestamp";
            case Object: return "object";
            case Array: return "array";
            default: return null;
        }
    }

    public static String dumpJsonSchema(SourceWriter sb, JdbcColumn col) {
        JsType jsType = JsType.of(col.getValueType());
        if (jsType == null) {
            throw new RuntimeException("JsonType not registered: " + col.getValueType() + " " + col.getPhysicalName());
        }
        sb.writeln("{")
                .incTab()
                .writeJsonKeyValue("key", col.getJsonKey())
                .writeJsonKeyValue("type", toFormType(jsType));

        if (col.isReadOnly()) {
            sb.writeJsonKeyValue("required", true);
        }
        sb.decTab();
        sb.write("}");
        return sb.toString();
    }

    public static String dumpJqlSchema(SourceWriter sb, JdbcColumn col) {
        String jsonType = getColumnType(col);
        if (jsonType == null) {
            throw new RuntimeException("JsonType not registered: " + col.getValueType() + " " + col.getPhysicalName());
        }
        sb.write("  hql.").write(jsonType).write("(\"")
                .write(col.getJsonKey()).write("\"");
        sb.write(")");

        if (col.isReadOnly()) {
            sb.write(".readOnly()");
        }
        else if (!col.isNullable()) {
            sb.write(".required()");
        }

        return sb.toString();
    }



    public static String createJoinJQL(QSchema schema) {
        SourceWriter sb = new SourceWriter('"');
        for (QColumn hqlColumn : schema.getReadableColumns()) {
            QColumn joined_pk = hqlColumn.getJoinedPrimaryColumn();
            if (joined_pk == null) continue;

            sb.write(schema.getSimpleName()).write("Schema.join(\"");
            sb.write(hqlColumn.getJsonKey()).write("\", ");
            sb.write(joined_pk.getSchema().getEntityType().getSimpleName()).write("Schema, \"");
            sb.write(joined_pk.getJsonKey()).write("\");\n");
        }
        return sb.toString();
    }

//    private static String getJoinName(String name) {
//        int i = name.lastIndexOf('_');
//        while (i > 0) {
//            if (name.charAt(--i) != '_') {
//                name = name.substring(0, i+1);
//                break;
//            }
//        }
//        return name;
//    }
//
    private static String getColumnType(QColumn column) {
        QColumn joinedPK = column.getJoinedPrimaryColumn();
        if (joinedPK != null) {
            String columnType = CaseConverter.toCamelCase(joinedPK.getSchema().getSimpleName(), true);
            return columnType;
        }

        Class javaType = column.getValueType();
        String type = ClassUtils.getBoxedType(javaType).getName();
        String colType = mdkTypes.get(type);
        if (colType == null) {
            colType = "Object";
            //throw new RuntimeException(type + " is not registered");
        }
        return colType;
    }

    private static void dumpColumnInfo(QColumn col, SourceWriter sb) {
        QColumn joinedPK = col.getJoinedPrimaryColumn();
        String columnType = getColumnType(col);
        if (!col.isNullable()) {
            columnType = columnType + '!';
        }
        sb.write(columnType).write(filler.substring(columnType.length()));
        sb.write(col.getJsonKey()).write('(').write(col.getPhysicalName()).write(')');
        if (col.isPrimaryKey()) {
            sb.write(" PK");
        }
        if (joinedPK != null) {
            sb.write(" FK -> ");
            sb.write(joinedPK.getSchema().getTableName());
            sb.write(".").write(joinedPK.getJsonKey());
        }

        sb.write("\n");
    }
    static String filler = "                                             ";
    public static String getSimpleSchema(QSchema schema, boolean withTableName) {
        SourceWriter sb = new SourceWriter('"');
        if (withTableName) {
            sb.write("==================================================\n");
            sb.write("# ").write(schema.getSimpleName()).write("\n");
        }
        else {
            sb.write("Type").write(filler.substring("Type".length())).write("Key(physical_column_name)\n");
        }
        sb.write("--------------------------------------------------\n");
        sb.write("// Leaf properties //\n");
        List<QColumn> primitiveColumns = schema.getBaseColumns();
        for (QColumn col : primitiveColumns) {
            dumpColumnInfo(col, sb);
        }

        boolean hasRef = primitiveColumns.size() != schema.getReadableColumns().size()
                || !schema.getEntityJoinMap().isEmpty();

        if (hasRef) {
            sb.write("\n// Reference properties //\n");

            for (QColumn col : schema.getExtendedColumns()) {
                dumpColumnInfo(col, sb);
            }

            for (Map.Entry<String, QJoin> entry : schema.getEntityJoinMap().entrySet()) {
                QJoin join = entry.getValue();
                QSchema refSchema = join.getTargetSchema();
                if (refSchema.hasOnlyForeignKeys()) continue;
                int start = sb.length();
                QSchema targetSchema = join.getTargetSchema();
                sb.write(join.getTargetSchema().generateEntityClassName());
                if (!join.hasUniqueTarget()) sb.write("[]");
                int type_len = sb.length() - start;
                if (type_len >= filler.length()) {
                    type_len = filler.length() - 1;
                }
                sb.write(filler.substring(type_len));
                sb.write(entry.getKey());
                sb.write("\n");
            }
        }
        return sb.toString();
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

    public static Object parseJson(ObjectMapper objectMapper, String json) {
        if (json == null || json.length() == 0) return json;
        Object value = json;
        try {
            Class<?> valueClass = switch (json.charAt(0)) {
                case '{' -> Map.class;
                case '[' -> List.class;
                default -> null;
            };
            if (valueClass != null) {
                value = objectMapper.readValue(json, valueClass);
            }
        } catch (JsonProcessingException e) {
            // mariadb longtext 인 경우;
        }
        return value;
    }
}
