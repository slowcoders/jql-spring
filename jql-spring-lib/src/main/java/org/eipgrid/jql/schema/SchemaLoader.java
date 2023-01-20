package org.eipgrid.jql.schema;

import org.eipgrid.jql.js.JsUtil;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SchemaLoader {

    private final HashMap<Class<?>, QSchema> classToSchemaMap = new HashMap<>();
    private final AttributeNameConverter nameConverter;

    protected SchemaLoader(AttributeNameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public final AttributeNameConverter getNameConverter() {
        return this.nameConverter;
    }

    public String getDefaultDBSchema() { return "public"; }

    public abstract QSchema loadSchema(String tablePath, Class<?> ormType);

    public QSchema loadSchema(Class<?> entityType) {
        QSchema schema = classToSchemaMap.get(entityType);
        if (schema == null) {
            String tableName = resolveTableName(entityType);
            schema = loadSchema(tableName, entityType);
            classToSchemaMap.put(entityType, schema);
        }
        return schema;
    }

    public String resolveTableName(Class<?> entityType) {
        String name = "";
        Table table = entityType.getAnnotation(Table.class);
        String schema = "";
        if (table != null) {
            name = table.name().trim();
            schema = table.schema().trim();
        }
        if (name.length() == 0) {
            name = entityType.getSimpleName();
        }
        return makeTablePath(schema, name);
    }

    public String makeTablePath(String schema, String name) {
        name = AttributeNameConverter.camelCaseConverter.toPhysicalColumnName(name).toLowerCase();
        if (schema == null || schema.length() == 0) {
            schema = getDefaultDBSchema();
        }
        schema = AttributeNameConverter.camelCaseConverter.toPhysicalColumnName(schema).toLowerCase();
        name = schema + "." + name;
        return name;
    }

//    public String toColumnType(Class<?> javaType, Field f) {
//        return JsUtil.getColumnType(javaType, joinedPK);
//    }


    protected abstract HashMap<String, QJoin> loadJoinMap(QSchema schema);
}
