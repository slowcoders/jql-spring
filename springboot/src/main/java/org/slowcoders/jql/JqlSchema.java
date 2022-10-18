package org.slowcoders.jql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.slowcoders.jql.jdbc.metadata.MappedColumn;

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
    protected final HashMap<String, JqlEntityJoin> tableJoinMap = new HashMap<>();

    public JqlSchema(SchemaLoader schemaLoader, String tableName, String jpaClassName) {
        this.tableName = tableName;
        this.schemaLoader = schemaLoader;
        this.jpaClassName = jpaClassName;
    }

    public final SchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public Class<?> getMappedClass() { return Map.class; }

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

    public JqlEntityJoin getJoinedColumnSet(String fieldName) {
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
        ArrayList<JqlColumn> pkColumns = new ArrayList<>();
        for (JqlColumn ci: columns) {
            if (ci.isPrimaryKey()) {
                pkColumns.add(ci);
            }

            String colName = ci.getColumnName().toLowerCase();
            this.columnMap.put(colName, ci);

            if (!ci.isReadOnly() && ci.getValueFormat().isPrimitive()) {
                writableColumns.add(ci);
            }
        }

        this.pkColumns = Collections.unmodifiableList(pkColumns);
        this.allColumns = Collections.unmodifiableList(columns);
        this.writableColumns = Collections.unmodifiableList(writableColumns);
    }

    protected void initJsonNames() {
        for (JqlColumn ci: allColumns) {
            String fieldName = ci.getJsonName();
            columnMap.put(fieldName, ci);
        }
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

    protected void initMappedColumns(String key, JqlEntityJoin mappedColumns) {
        this.tableJoinMap.put(key, mappedColumns);
    }

    public boolean isUnique(List<JqlColumn> fkColumns) {
        return false;
    }

    public String toString() {
        return this.tableName;
    }
}
