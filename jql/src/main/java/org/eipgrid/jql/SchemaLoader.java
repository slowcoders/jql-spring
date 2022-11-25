package org.eipgrid.jql;

import org.eipgrid.jql.jpa.JpaSchema;
import org.eipgrid.jql.json.JsonJql;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SchemaLoader {

    private final HashMap<Class<?>, JqlSchema> classToSchemaMap = new HashMap<>();
    private final AttributeNameConverter nameConverter;
    private AtomicInteger cntSchema = new AtomicInteger();

    protected SchemaLoader(AttributeNameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public final AttributeNameConverter getNameConverter() {
        return this.nameConverter;
    }

    public String getDefaultDBSchema() { return "public"; }

    public JqlSchema loadSchema(Class<?> entityType) {
        JqlSchema schema = classToSchemaMap.get(entityType);
        if (schema == null) {
            String tableName = resolveTableName(entityType);
            if (tableName == null) {
                throw new RuntimeException("@Table not found");
            }
            return loadSchema(entityType, tableName);
        }
        return schema;
    }

    public JqlSchema loadSchema(Class<?> entityType, String tableName) {
        JqlSchema schema = new JpaSchema(this, tableName, entityType);
        classToSchemaMap.put(entityType, schema);
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

    public abstract JqlSchema loadSchema(String tablePath);

    public abstract String createDDL(JqlSchema schema);

    public String toColumnType(Class<?> javaType, Field f) {
        return JsonJql.getColumnType(javaType);
    }

    protected String generateUniqueAlias(JqlSchema schema) {
        int id = cntSchema.getAndIncrement();
        String alias = (id < 10 ? "t_" : "t") + id;
        return alias;
    }

    protected abstract HashMap<String, JqlSchemaJoin> loadJoinMap(JqlSchema jqlSchema);
}
