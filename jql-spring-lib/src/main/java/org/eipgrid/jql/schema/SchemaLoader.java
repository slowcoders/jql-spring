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

    public String makeTablePath(String schema, String name) {
        name = CaseConverter.camelCaseConverter.toPhysicalColumnName(name).toLowerCase();
        if (schema == null || schema.length() == 0) {
            schema = getDefaultDBSchema();
        }
        schema = CaseConverter.camelCaseConverter.toPhysicalColumnName(schema).toLowerCase();
        name = schema + "." + name;
        return name;
    }

//    public String toColumnType(Class<?> javaType, Field f) {
//        return JsUtil.getColumnType(javaType, joinedPK);
//    }


    protected abstract HashMap<String, QJoin> loadJoinMap(QSchema schema);
}
