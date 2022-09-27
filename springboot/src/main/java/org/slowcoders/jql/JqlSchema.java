package org.slowcoders.jql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.jdbc.metadata.JqlRowMapper;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.jdbc.core.RowMapper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JqlSchema {
    private final SchemaLoader schemaLoader;
    private final String tableName;
    private String jpaClassName;

    private List<JqlColumn> allColumns;
    private List<JqlColumn> writableColumns;
    private HashMap<String, JqlColumn> columnMap = new HashMap<>();
    protected final HashMap<String, List<JqlColumn>> tableJoinMap = new HashMap<>();

    private ArrayList<String> primaryKeys;

    public JqlSchema(SchemaLoader schemaLoader, String tableName, String jpaClassName) {
        this.tableName = tableName;
        this.schemaLoader = schemaLoader;
        this.jpaClassName = jpaClassName;
    }

    public final SchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public JqlColumn getTimeKeyColumn() {
        for (String key : this.primaryKeys) {
            JqlColumn column = this.getColumn(key);
            if (column.getValueFormat() == ValueFormat.Timestamp) {
                return column;
            }
        }
        return null;
    }

    public void init(ArrayList<? extends JqlColumn> columns, ArrayList<String> primaryKeys, HashMap<String, JqlSchemaJoin> tableJoinMap2) {
        this.allColumns = Collections.unmodifiableList(columns);
        this.primaryKeys = primaryKeys;
//        this.tableJoinMap = tableJoinMap;
        ArrayList<JqlColumn> writableColumns = new ArrayList<>();
        for (JqlColumn ci: columns) {

            JqlColumn joined_pk = ci.getJoinedPrimaryColumn();
            if (joined_pk != null) {
                String joinFieldName = joined_pk.getSchema().getJpaClassName();
                List<JqlColumn> foreignKeys = tableJoinMap.get(joinFieldName);
                if (foreignKeys == null) {
                    foreignKeys = new ArrayList<>();
                    tableJoinMap.put(joinFieldName, foreignKeys);
                }
                foreignKeys.add(ci);
            }

            String fieldName = ci.getFieldName();
            columnMap.put(ci.getFieldName(), ci);
            String colName = ci.getColumnName().toLowerCase();
            if (!fieldName.equals(colName)) {
                columnMap.put(colName, ci);
            }

            if (!ci.isReadOnly() &&
                    ci.getValueFormat() != ValueFormat.Collection &&
                    ci.getValueFormat() != ValueFormat.Embedded) {
                writableColumns.add(ci);
            }
        }
        this.writableColumns = Collections.unmodifiableList(writableColumns);
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<JqlColumn> getJoinedForeignKeys(String fieldName) {
        return tableJoinMap.get(fieldName);
    }

//    public Collection<JqlSchemaJoin> getJoinList() {
//        return tableJoinMap.values();
//    }

    public String dumpJPAEntitySchema() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        out.println("import lombok.*;");
        out.println("import javax.persistence.*;");
        out.println("import javax.validation.constraints.*;");
        out.println();

        out.println("public class " + tableName + " {");
        for (JqlColumn col : allColumns) {
            dumpORM(col, out);
            out.println();
        }
        out.println("}");
        return baos.toString();
    }

    private void dumpORM(JqlColumn col, PrintStream out) {
//        if (col.getLabel() != null) {
//            out.print("\t/** ");
//            out.print(col.getLabel());
//            out.println(" */");
//        }
//        if (primaryKeys.contains(col.getColumnName())) {
//            out.println("\t@Id");
//            if (col.isAutoIncrement()) {
//                out.println("\t@GeneratedValue(strategy = GenerationType.IDENTITY)");
//            }
//        }
//        if (!col.isNullable()) {
//            out.println("\t@NotNull");
//            out.println("\t@Column(nullable = false)");
//        }
//        if (col.getPrecision() > 0) {
//            out.println("\t@Max(" + col.getPrecision() +")");
//        }

        out.print("\t@Getter");
        if (!col.isReadOnly()) {
            out.print(" @Setter");
        }
        out.println();

        out.println("\t" + col.getJavaType().getName() + " " + col.getFieldName() + ";");
    }

    @JsonIgnore
    public List<JqlColumn> getPKColumns() {
        ArrayList<JqlColumn> pkColumns = new ArrayList<>();
        for (String key: this.primaryKeys) {
            pkColumns.add(this.getColumn(key));
        }
        return pkColumns;
    }

    @JsonIgnore
    public List<String> getPrimaryKeys() {
        return this.primaryKeys;
    }

    public boolean hasColumn(String key) {
        return columnMap.containsKey(key);
    }

    public JqlColumn getColumn(String key) {
        JqlColumn ci = columnMap.get(key);
        if (ci == null) {
            throw new IllegalArgumentException("unknown column: " + this.tableName + "." + key);
        }
        return ci;
    }

    public Map<String, Object> separateUnknownColumns(Map<String, Object> metric)  {
        HashMap<String, Object> unknownProperties = null;
        for (Map.Entry<String, Object> entry : metric.entrySet()) {
            String key = entry.getKey();
            if (!this.columnMap.containsKey(key)
             && !this.columnMap.containsKey(key.toLowerCase())) {
                if (unknownProperties == null) {
                    unknownProperties = new HashMap<>();
                }
                Object value = entry.getValue();
                unknownProperties.put(key, value);
            }
        }
        try {
            if (unknownProperties != null) {
                for (String key : unknownProperties.keySet()) {
                    metric.remove(key);
                }
//                String p$ = objectMapper.writeValueAsString(unknownProperties);
//                metric.put(jsonColumn.getFieldName(), p$);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return unknownProperties;
    }

    //    @JsonIgnore
    public Iterable<JqlColumn> getReadableColumns() {
        return (Iterable) this.allColumns;
    }

    @JsonIgnore
    public List<JqlColumn> getWritableColumns() {
        return (List)writableColumns;
    }


    public Object extractEntityId(Map<String, Object> entity, Map<String, Object> generatedKeys) {
        if (primaryKeys == null || primaryKeys.size() == 0) return null;
        if (primaryKeys.size() > 1) {
            Object[] id = new Object[primaryKeys.size()];
            int i = 0;
            for (String key : primaryKeys) {
                Object v = getValue(key, entity, generatedKeys);
                id[i++] = v;
            }
            return id;
        }
        else {
            Object id = getValue(primaryKeys.get(0), entity, generatedKeys);
            return id;
        }
    }

    private static Object getValue(String key, Map<String, Object> entity, Map<String, Object> generatedKeys) {
        if (generatedKeys != null && generatedKeys.containsKey(key)) {
            return generatedKeys.get(key);
        }
        return entity.get(key);
    }

    public String generateDDL() {
        return schemaLoader.createDDL(this);
    }

    public String getBaseTableName() {
        return this.tableName.substring(this.tableName.indexOf('.') + 1);
    }

    public String getJpaClassName() {
        return this.jpaClassName;
    }

    public String getPhysicalColumnName(String fieldName) {
        return this.columnMap.get(fieldName).getColumnName();
    }

    public String getLogicalAttributeName(String columnName) {
        return this.columnMap.get(columnName).getFieldName();
    }
    public String[] getPhysicalColumnNames(String[] fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) return null;
        String[] out = new String[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i ++) {
            String key = fieldNames[i];
            out[i] = getPhysicalColumnName(key);
        }
        return out;
    }

    public List<String> getPhysicalColumnNames(Iterable<String> fieldNames) {
        ArrayList<String> out = new ArrayList<>();
        for (String key : fieldNames) {
            out.add(getPhysicalColumnName(key));
        }
        return out;
    }
}
