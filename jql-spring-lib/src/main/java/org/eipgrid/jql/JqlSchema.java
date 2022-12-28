package org.eipgrid.jql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.lang.reflect.Field;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class JqlSchema {
    private final SchemaLoader schemaLoader;
    private final String tableName;
    private final String alias;
    private String jpaClassName;

    private List<JqlColumn> pkColumns;
    private List<JqlColumn> allColumns;
    private List<JqlColumn> writableColumns;
    private Map<String, JqlColumn> columnMap = new HashMap<>();
    private HashMap<String, JqlEntityJoin> entityJoinMap;

    public JqlSchema(SchemaLoader schemaLoader, String tableName, String jpaClassName) {
        this.tableName = tableName;
        this.schemaLoader = schemaLoader;
        this.jpaClassName = jpaClassName;
        this.alias = schemaLoader.generateUniqueAlias(this);
    }

    public final SchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getAlias() {
        return this.alias;
    }

    public List<JqlColumn> getReadableColumns() {
        return this.allColumns;
    }

    public List<JqlColumn> getWritableColumns() {
        return (List)writableColumns;
    }

    public List<JqlColumn> getPKColumns() {
        return this.pkColumns;
    }

    public JqlEntityJoin getEntityJoinBy(String jsonKey) {
        return getEntityJoinMap().get(jsonKey);
    }

    public JqlColumn findColumn(String key) throws IllegalArgumentException {
        return columnMap.get(key);
    }

    public JqlColumn getColumn(String key) throws IllegalArgumentException {
        JqlColumn ci = findColumn(key);
        if (ci == null) {
            throw new IllegalArgumentException("unknown column: " + this.tableName + "." + key);
        }
        return ci;
    }

    public boolean hasColumn(String key) {
        return columnMap.get(key) != null;
    }

    public String getSimpleTableName() {
        return this.tableName.substring(this.tableName.indexOf('.') + 1);
    }

    public String getEntityClassName() {
        return this.jpaClassName;
    }

    protected void init(ArrayList<? extends JqlColumn> columns, Class<?> ormType) {
        ArrayList<JqlColumn> writableColumns = new ArrayList<>();
        ArrayList<JqlColumn> allColumns = new ArrayList<>();
        List<JqlColumn> pkColumns = new ArrayList<>();

        for (JqlColumn ci: columns) {
            if (ci.isPrimaryKey()) {
                pkColumns.add(ci);
                allColumns.add(ci);
            }
        }

        for (JqlColumn ci: columns) {
            String colName = ci.getColumnName().toLowerCase();
            this.columnMap.put(colName, ci);

            if (!ci.isPrimaryKey()) {
                allColumns.add(ci);
            }
            if (!ci.isReadOnly() && ci.getValueKind().isPrimitive()) {
                writableColumns.add(ci);
            }
        }

        this.allColumns = Collections.unmodifiableList(allColumns);
        this.writableColumns = Collections.unmodifiableList(writableColumns);
        this.initJsonKeys(ormType);
        if (pkColumns.size() == 0) {
            pkColumns = getAlternativeIdColumns();
        }
        this.pkColumns = Collections.unmodifiableList(pkColumns);
    }

    protected void mapColumn(JqlColumn column, Field f) {
        column.setMappedField(f);
    }
    
    protected List<JqlColumn> getAlternativeIdColumns() {
        return this.allColumns;
    }

    protected void initJsonKeys(Class<?> ormType) {
        for (JqlColumn ci : allColumns) {
            String fieldName = ci.getJsonKey();
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


//    public String generateDDL() {
//        return schemaLoader.createDDL(this);
//    }

    public HashMap<String, JqlEntityJoin> getEntityJoinMap() {
        if (this.entityJoinMap == null) {
            this.entityJoinMap = schemaLoader.loadJoinMap(this);
        }
        return this.entityJoinMap;
    }
    //==========================================================================
    // Attribute Name Conversion
    //--------------------------------------------------------------------------

    public String getPhysicalColumnName(String fieldName) {
        return this.columnMap.get(fieldName).getColumnName();
    }

    public String getLogicalAttributeName(String columnName) {
        return this.columnMap.get(columnName).getJsonKey();
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

    public boolean isUniqueConstrainedColumnSet(List<JqlColumn> fkColumns) {
        return false;
    }

    public String toString() {
        return this.tableName;
    }

    public String getNamespace() {
        String tableName = this.getTableName();
        int p = tableName.lastIndexOf('.');
        return p < 0 ? "" : tableName.substring(0, p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JqlSchema schema = (JqlSchema) o;
        return tableName.equals(schema.tableName);
    }

    @Override
    public int hashCode() {
        return tableName.hashCode();
    }
}
