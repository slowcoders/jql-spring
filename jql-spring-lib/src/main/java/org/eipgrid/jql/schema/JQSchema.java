package org.eipgrid.jql.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.lang.reflect.Field;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class JQSchema {
    private final JQSchemaLoader schemaLoader;
    private final String tableName;

    private String jpaClassName;

    private List<JQColumn> pkColumns;
    private List<JQColumn> allColumns;
    private List<JQColumn> writableColumns;
    private Map<String, JQColumn> columnMap = new HashMap<>();
    private HashMap<String, JQJoin> entityJoinMap;

    public JQSchema(JQSchemaLoader schemaLoader, String tableName, String jpaClassName) {
        this.tableName = tableName;
        this.schemaLoader = schemaLoader;
        this.jpaClassName = jpaClassName;
    }

    public final JQSchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<JQColumn> getReadableColumns() {
        return this.allColumns;
    }

    public List<JQColumn> getWritableColumns() {
        return (List)writableColumns;
    }

    public List<JQColumn> getPKColumns() {
        return this.pkColumns;
    }

    public JQJoin getEntityJoinBy(String jsonKey) {
        return getEntityJoinMap().get(jsonKey);
    }

    public JQColumn findColumn(String key) throws IllegalArgumentException {
        return columnMap.get(key);
    }

    public JQColumn getColumn(String key) throws IllegalArgumentException {
        JQColumn ci = findColumn(key);
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

    protected void init(ArrayList<? extends JQColumn> columns, Class<?> ormType) {
        ArrayList<JQColumn> writableColumns = new ArrayList<>();
        ArrayList<JQColumn> allColumns = new ArrayList<>();
        List<JQColumn> pkColumns = new ArrayList<>();

        for (JQColumn ci: columns) {
            if (ci.isPrimaryKey()) {
                pkColumns.add(ci);
                allColumns.add(ci);
            }
        }

        for (JQColumn ci: columns) {
            String colName = ci.getPhysicalName().toLowerCase();
            this.columnMap.put(colName, ci);

            if (!ci.isPrimaryKey()) {
                allColumns.add(ci);
            }
            if (!ci.isReadOnly() && ci.getType().isPrimitive()) {
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

    protected void mapColumn(JQColumn column, Field f) {
        column.setMappedField(f);
    }
    
    protected List<JQColumn> getAlternativeIdColumns() {
        return this.allColumns;
    }

    protected void initJsonKeys(Class<?> ormType) {
        for (JQColumn ci : allColumns) {
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

    public HashMap<String, JQJoin> getEntityJoinMap() {
        if (this.entityJoinMap == null) {
            this.entityJoinMap = schemaLoader.loadJoinMap(this);
        }
        return this.entityJoinMap;
    }
    //==========================================================================
    // Attribute Name Conversion
    //--------------------------------------------------------------------------

    public String getPhysicalColumnName(String fieldName) {
        return this.columnMap.get(fieldName).getPhysicalName();
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

    public boolean isUniqueConstrainedColumnSet(List<JQColumn> fkColumns) {
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
        JQSchema schema = (JQSchema) o;
        return tableName.equals(schema.tableName);
    }

    @Override
    public int hashCode() {
        return tableName.hashCode();
    }
}
