package org.slowcoders.hyperql.schema;

import org.slowcoders.hyperql.AutoSelectable;
import org.slowcoders.hyperql.HyperRepository;
import org.slowcoders.hyperql.HyperStorage;

import java.lang.reflect.Field;
import java.util.*;

public abstract class QSchema {
    private final String tableName;
    private final String simpleName;
    private final Class<?> entityType;

    private List<QColumn> pkColumns;
    private List<QColumn> allColumns;
    private List<QColumn> baseColumns;
    private List<QColumn> extendedColumns;
    private List<QColumn> writableColumns;
    private Map<String, QColumn> columnMap = new HashMap<>();
    private boolean hasGeneratedId;
    private final boolean isJPASchema;

    public QSchema(String tableName, Class<?> entityType) {
        if (entityType == null) entityType = HyperRepository.rawEntityType;
        this.tableName = tableName;
        this.entityType = entityType;
        this.isJPASchema = !HyperRepository.rawEntityType.isAssignableFrom(entityType);
        String simpleName = tableName.substring(tableName.lastIndexOf('.') + 1);
        if (simpleName.charAt(0) == '"') {
            simpleName = simpleName.substring(1, simpleName.length() - 1);
        }
        this.simpleName = simpleName;
    }

    public abstract HyperStorage getStorage();

    public final boolean isJPARequired() { return this.isJPASchema; }

    public final Class<?> getEntityType() { return entityType; }

    public Class<?> getIdType() { return Object.class; }

    public final String getTableName() {
        return this.tableName;
    }

    public String getTableExpression(String[] params) {
        return this.tableName;
    }

    public final String getSimpleName() { return this.simpleName; }

    public final String generateEntityClassName() {
        return getStorage().toEntityClassName(this.getSimpleName(), true);
    }


    public List<QColumn> getReadableColumns() {
        return this.allColumns;
    }

    public List<QColumn> getBaseColumns() {
        return this.baseColumns;
    }

    public List<QColumn> getExtendedColumns() {
        return this.extendedColumns;
    }

    public List<QColumn> getWritableColumns() {
        return (List)writableColumns;
    }

    public List<QColumn> getPKColumns() {
        return this.pkColumns;
    }

    public QJoin getEntityJoinBy(String jsonKey) {
        return getEntityJoinMap().get(jsonKey);
    }

    public QColumn findColumn(String key) throws IllegalArgumentException {
        QColumn ci = columnMap.get(key);
        if (ci == null) {
            ci = columnMap.get(key.toLowerCase());
        }
        return ci;
    }

    public QColumn getColumn(String key) throws IllegalArgumentException {
        QColumn ci = findColumn(key);
        if (ci == null) {
            throw new IllegalArgumentException("unknown column [" + this.tableName + "::" + key + "]");
        }
        return ci;
    }

    public boolean hasColumn(String key) {
        return columnMap.get(key) != null;
    }

    protected void init(ArrayList<? extends QColumn> columns, Class<?> ormType) {
        ArrayList<QColumn> writableColumns = new ArrayList<>();
        ArrayList<QColumn> allColumns = new ArrayList<>();
        ArrayList<QColumn> primitiveColumns = new ArrayList<>();
        ArrayList<QColumn> extendedColumns = new ArrayList<>();
        List<QColumn> pkColumns = new ArrayList<>();

        boolean hasGeneratedId = false;
        for (QColumn ci: columns) {
            this.columnMap.put(ci.getPhysicalName().toLowerCase(), ci);

            if (ci.isPrimaryKey()) {
                pkColumns.add(ci);
                hasGeneratedId |= ci.isAutoIncrement();
            }
            else {
                allColumns.add(ci);
                if (!ci.isForeignKey()) {
                    Field f = ci.getMappedOrmField();
                    AutoSelectable selectable;

                    boolean autoSelectable = !ci.isJsonNode();
                    if (f != null && (selectable = f.getAnnotation(AutoSelectable.class)) != null) {
                        autoSelectable = selectable.value();
                    } else if (autoSelectable) {
                        String name = (f != null) ? f.getName() : ci.getPhysicalName();
                        autoSelectable = name.charAt(0) != '_';
                    }

                    if (!autoSelectable) {
                        extendedColumns.add(ci);
                    }
                    else {
                        primitiveColumns.add(ci);
                    }
                }
            }

            if (/*!ci.isForeignKey() &&*/ !ci.isReadOnly()) {
                writableColumns.add(ci);
                /**
                 * 참고) ForeignKey 를 writableColumns 에 등록하지 않으면,
                 * Insert 시 fk 를 아래와 같이 구조화하여 요청하여야 한다.
                 * {
                 *    master { id = 333 }
                 * }
                 */
            }
        }

        allColumns.addAll(0, pkColumns);
        primitiveColumns.addAll(0, pkColumns);
        this.hasGeneratedId = hasGeneratedId;
        this.allColumns = Collections.unmodifiableList(allColumns);
        this.writableColumns = Collections.unmodifiableList(writableColumns);
        this.baseColumns = Collections.unmodifiableList(primitiveColumns);
        this.extendedColumns = extendedColumns.size() == 0 ? Collections.EMPTY_LIST : Collections.unmodifiableList(extendedColumns);
        this.initJsonKeys(ormType);
        if (pkColumns.size() == 0) {
            pkColumns = this.allColumns;
            markAllColumnsToPK(pkColumns);
        }
        this.pkColumns = Collections.unmodifiableList(pkColumns);
    }

    protected void markAllColumnsToPK(List<QColumn> pkColumns) {
        throw new RuntimeException("not implemented");
    }

    protected void mapColumn(QColumn column, Field f) {
        column.setMappedOrmField(f);
    }

    protected void initJsonKeys(Class<?> ormType) {
        for (QColumn ci : allColumns) {
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


    public Map<String, QJoin> getEntityJoinMap() {
        return Collections.EMPTY_MAP;
    }
    //==========================================================================
    // Attribute Name Conversion
    //--------------------------------------------------------------------------

    public static String getJavaFieldName(QColumn column) {
        String name = column.getJsonKey();
        int idx = name.indexOf('.');
        if (idx > 0) {
            name = name.substring(0, idx);
        }
        return name;
    }


    public boolean isUniqueConstrainedColumnSet(List<QColumn> fkColumns) {
        return false;
    }

    public String toString() {
        return this.tableName;
    }

    public String getNamespace() {
        String tableName = this.getTableName();
        int p = tableName.lastIndexOf('.');
        return p < 0 ? null : tableName.substring(0, p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QSchema schema = (QSchema) o;
        return tableName.equals(schema.tableName);
    }

    @Override
    public int hashCode() {
        return tableName.hashCode();
    }

    public boolean hasGeneratedId() {
        return this.hasGeneratedId;
    }

    public boolean hasOnlyForeignKeys() {
        for (QColumn col : getReadableColumns()) {
            if (col.getJoinedPrimaryColumn() == null) {
                return false;
            }
        }
        return true;
    }


    public abstract <ID, ENTITY> ID getEnityId(ENTITY entity);

    public final boolean hasProperty(String name) {
        return this.findColumn(name) != null ||
                this.getEntityJoinBy(name) != null;
    }
}
