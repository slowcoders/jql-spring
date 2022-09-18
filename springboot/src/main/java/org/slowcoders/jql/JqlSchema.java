package org.slowcoders.jql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.jql.jdbc.metadata.JqlRowMapper;
import org.slowcoders.jql.jpa.JpaColumn;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.jdbc.core.RowMapper;

import javax.persistence.Transient;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JqlSchema {
    private final SchemaLoader schemaLoader;
    private final String tableName;
    private String jpaClassName;

    private List<JqlColumn> allColumns;
    private List<JqlColumn> writableColumns;
    private HashMap<String, JqlColumn> fieldNameMap = new HashMap<>();
    private HashMap<String, JqlSchemaJoin> tableJoinMap = new HashMap<>();

    private JqlColumn jsonColumn;
    private ArrayList<String> primaryKeys;

    public JqlSchema(SchemaLoader schemaLoader, String tableName) {
        this.tableName = tableName;
        this.schemaLoader = schemaLoader;
        this.jpaClassName = AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(this.getBaseTableName());
    }

    protected void init(Class<?> entityType) {
        ArrayList<JqlColumn> columns = new ArrayList<>();
        this.jpaClassName = entityType.getTypeName();
        this.initColumns(columns, entityType);
        this.init(columns, null);
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

    /*package*/ void initColumns(ArrayList<JqlColumn> columns, Class<?> entityType) {
        Class<?> superClass = entityType.getSuperclass();
        if (superClass != Object.class) {
            initColumns(columns, superClass);
        }
        for (Field f : entityType.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.TRANSIENT) == 0 &&
                f.getAnnotation(Transient.class) != null) {
                JqlColumn col = new JpaColumn(f, this);
                columns.add(col);
            }
        }
    }

    public void init(ArrayList<JqlColumn> columns, ArrayList<String> primaryKeys) {
        this.allColumns = Collections.unmodifiableList(columns);
        this.primaryKeys = primaryKeys;
        ArrayList<JqlColumn> writableColumns = new ArrayList<>();
        for (JqlColumn ci: columns) {

            JqlColumnJoin fk = ci.getJoinedForeignKey();
            if (fk != null) {
                String joinFieldName = fk.getJoinedFieldName();
                JqlSchemaJoin foreignKeys = tableJoinMap.get(joinFieldName);
                if (foreignKeys == null) {
                    foreignKeys = new JqlSchemaJoin(this.schemaLoader);
                    tableJoinMap.put(joinFieldName, foreignKeys);
                }
                foreignKeys.add(fk);
            }

            String fieldName = ci.getFieldName();
            fieldNameMap.put(ci.getFieldName(), ci);
            String colName = ci.getColumnName().toLowerCase();
            if (!fieldName.equals(colName)) {
                fieldNameMap.put(colName, ci);
            }
            if (ci.getJavaType() == Object.class || Map.class.isAssignableFrom(ci.getJavaType()) ||
                    "json".equals(ci.getDBColumnType()) || "jsonb".equals(ci.getDBColumnType())) {
                this.jsonColumn = ci;
            }

            if (!ci.isAutoIncrement() &&
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

    public JqlSchemaJoin getJoinedForeignKeys(String fieldName) {
        return tableJoinMap.get(fieldName);
    }

    public Collection<JqlSchemaJoin> getJoinList() {
        return tableJoinMap.values();
    }

    public String dumpJPAEntitySchema() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        out.println("import lombok.*;");
        out.println("import javax.persistence.*;");
        out.println("import javax.validation.constraints.*;");
        out.println();

        out.println("public class " + tableName + " {");
        for (JqlColumn col : allColumns) {
            col.dumpORM(out);
            out.println();
        }
        out.println("}");
        return baos.toString();
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
        return fieldNameMap.containsKey(key);
    }

    public JqlColumn getColumn(String key) {
        JqlColumn ci = fieldNameMap.get(key);
        if (ci == null) {
            throw new IllegalArgumentException("unknown column: " + this.tableName + "." + key);
        }
        return ci;
    }

    public Map autoArrangeColumns(Map<String, Object> metric, ObjectMapper objectMapper)  {
        if (jsonColumn != null) {
            HashMap<String, Object> properties = null;
            for (Map.Entry<String, Object> entry : metric.entrySet()) {
                String key = entry.getKey();
                if (!this.fieldNameMap.containsKey(key)
                 && !this.fieldNameMap.containsKey(key.toLowerCase())) {
                    if (properties == null) {
                        properties = new HashMap<>();
                    }
                    Object value = entry.getValue();
                    properties.put(key, value);
                }
            }
            try {
                if (properties != null) {
                    for (String key : properties.keySet()) {
                        metric.remove(key);
                    }
                    String p$ = objectMapper.writeValueAsString(properties);
                    metric.put(jsonColumn.getFieldName(), p$);
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return metric;
    }

    //    @JsonIgnore
    public Iterable<JqlColumn> getReadableColumns() {
        return this.allColumns;
    }

    @JsonIgnore
    public List<JqlColumn> getWritableColumns() {
        return writableColumns;
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

    public RowMapper<KVEntity> getColumnMapRowMapper() {
        return new JqlRowMapper(this);
    }

    public String getBaseTableName() {
        return this.tableName.substring(this.tableName.indexOf('.') + 1);
    }

    public String getJpaClassName() {
        return this.jpaClassName;
    }

    public String getPhysicalColumnName(String fieldName) {
        return this.fieldNameMap.get(fieldName).getColumnName();
    }

    public String getLogicalAttributeName(String columnName) {
        return this.fieldNameMap.get(columnName).getFieldName();
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
