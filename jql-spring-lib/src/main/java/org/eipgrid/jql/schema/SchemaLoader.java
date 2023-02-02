package org.eipgrid.jql.schema;

import org.eipgrid.jql.util.CaseConverter;

import java.util.HashMap;

public abstract class SchemaLoader {

    private final HashMap<Class<?>, QSchema> classToSchemaMap = new HashMap<>();
    private final CaseConverter nameConverter;

    protected SchemaLoader(CaseConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public final CaseConverter getNameConverter() {
        return this.nameConverter;
    }

    public String getDefaultDBSchema() { return "public"; }

    public abstract QSchema loadSchema(String tablePath, Class<?> ormType);

    public QSchema loadSchema(Class<?> entityType) {
        QSchema schema = classToSchemaMap.get(entityType);
        if (schema == null) {
            schema = loadSchema(null, entityType);
            classToSchemaMap.put(entityType, schema);
        }
        return schema;
    }

    protected abstract HashMap<String, QJoin> loadJoinMap(QSchema schema);
}
