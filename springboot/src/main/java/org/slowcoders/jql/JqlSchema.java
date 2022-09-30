package org.slowcoders.jql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JqlSchema {
    private final SchemaLoader schemaLoader;
    private final String tableName;
    private String jpaClassName;

    private List<JqlColumn> pkColumns;
    private List<JqlColumn> allColumns;
    private List<JqlColumn> writableColumns;
    private Map<String, JqlColumn> columnMap = new HashMap<>();
    protected final HashMap<String, List<JqlColumn>> tableJoinMap = new HashMap<>();

    public JqlSchema(SchemaLoader schemaLoader, String tableName, String jpaClassName) {
        this.tableName = tableName;
        this.schemaLoader = schemaLoader;
        this.jpaClassName = jpaClassName;
    }

    public final SchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public String getTableName() {
        return this.tableName;
    }

    public Iterable<JqlColumn> getReadableColumns() {
        return (Iterable) this.allColumns;
    }

    public List<JqlColumn> getWritableColumns() {
        return (List)writableColumns;
    }

    public List<JqlColumn> getPKColumns() {
        return this.pkColumns;
    }

    public List<JqlColumn> getJoinedColumnSet(String fieldName) {
        return tableJoinMap.get(fieldName);
    }

    public Iterable<String> getJoinedFieldNames() {
        return tableJoinMap.keySet();
    }

    public JqlColumn getColumn(String key) throws IllegalArgumentException {
        JqlColumn ci = columnMap.get(key);
        if (ci == null) {
            throw new IllegalArgumentException("unknown column: " + this.tableName + "." + key);
        }
        return ci;
    }

    public boolean hasColumn(String key) {
        return columnMap.get(key) != null;
    }

    public String getBaseTableName() {
        return this.tableName.substring(this.tableName.indexOf('.') + 1);
    }

    public String getEntityClassName() {
        return this.jpaClassName;
    }

    protected void init(ArrayList<? extends JqlColumn> columns) {
        ArrayList<JqlColumn> writableColumns = new ArrayList<>();
        HashMap<String, JqlColumn> columnMap = new HashMap<>();
        ArrayList<JqlColumn> pkColumns = new ArrayList<>();
        for (JqlColumn ci: columns) {
            JqlColumn joined_pk = ci.getJoinedPrimaryColumn();
            if (joined_pk != null) {
                String joinFieldName = joined_pk.getSchema().getEntityClassName();
                List<JqlColumn> foreignKeys = tableJoinMap.get(joinFieldName);
                if (foreignKeys == null) {
                    foreignKeys = new ArrayList<>();
                    tableJoinMap.put(joinFieldName, foreignKeys);
                }
                foreignKeys.add(ci);
            }

            if (ci.isPrimaryKey()) {
                pkColumns.add(ci);
            }

            String fieldName = ci.getJsonName();
            columnMap.put(fieldName, ci);
            String colName = ci.getColumnName().toLowerCase();
            if (!fieldName.equals(colName)) {
                columnMap.put(colName, ci);
            }

            if (!ci.isReadOnly() && ci.getValueFormat().isPrimitive()) {
                writableColumns.add(ci);
            }
        }
        this.pkColumns = Collections.unmodifiableList(pkColumns);
        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.allColumns = Collections.unmodifiableList(columns);
        this.writableColumns = Collections.unmodifiableList(writableColumns);
    }

    public Map<String, Object> splitUnknownProperties(Map<String, Object> metric)  {
        HashMap<String, Object> unknownProperties = new HashMap<>();
        for (Map.Entry<String, Object> entry : metric.entrySet()) {
            String key = entry.getKey();
            if (!this.columnMap.containsKey(key) &&
                !this.columnMap.containsKey(key.toLowerCase())) {
                unknownProperties.put(key, entry.getValue());
            }
        }
        for (String key : unknownProperties.keySet()) {
            metric.remove(key);
        }
        return unknownProperties;
    }


    public String generateDDL() {
        return schemaLoader.createDDL(this);
    }

    //==========================================================================
    // Attribute Name Conversion
    //--------------------------------------------------------------------------

    public String getPhysicalColumnName(String fieldName) {
        return this.columnMap.get(fieldName).getColumnName();
    }

    public String getLogicalAttributeName(String columnName) {
        return this.columnMap.get(columnName).getJsonName();
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
