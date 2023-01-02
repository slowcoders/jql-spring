package org.eipgrid.jql;

import org.eipgrid.jql.js.JsUtil;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class JQSchemaLoader {

    private final HashMap<Class<?>, JQSchema> classToSchemaMap = new HashMap<>();
    private final AttributeNameConverter nameConverter;
    private AtomicInteger cntSchema = new AtomicInteger();

    protected JQSchemaLoader(AttributeNameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public final AttributeNameConverter getNameConverter() {
        return this.nameConverter;
    }

    public String getDefaultDBSchema() { return "public"; }

    public abstract JQSchema loadSchema(String tablePath, Class<?> ormType);

    public JQSchema loadSchema(Class<?> entityType) {
        JQSchema schema = classToSchemaMap.get(entityType);
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

    public String toColumnType(Class<?> javaType, Field f) {
        return JsUtil.getColumnType(javaType);
    }

    protected String generateUniqueAlias(JQSchema schema) {
        int id = cntSchema.getAndIncrement();
        String alias = (id < 10 ? "t_" : "t") + id;
        return alias;
    }

    protected abstract HashMap<String, JQJoin> loadJoinMap(JQSchema schema);
}
