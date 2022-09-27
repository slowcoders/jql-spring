package org.slowcoders.jql.jpa;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.*;
import org.slowcoders.jql.jdbc.metadata.JdbcColumn;
import org.slowcoders.jql.jdbc.timescale.Aggregate;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.slowcoders.jql.util.ClassUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Max;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

public class JpaSchema extends JqlSchema {
    public JpaSchema(SchemaLoader schemaLoader, String tableName, Class<?> entityType) {
        super(schemaLoader, tableName, entityType.getTypeName());

        ArrayList<JpaColumn> columns = new ArrayList<>();
        this.initColumns(columns, entityType);
        this.init(columns, null, getTableJoinMap(columns));
    }

    /*package*/ void initColumns(ArrayList<JpaColumn> columns, Class<?> entityType) {
        Class<?> superClass = entityType.getSuperclass();
        if (superClass != Object.class) {
            initColumns(columns, superClass);
        }
        for (Field f : entityType.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.TRANSIENT) == 0 &&
                    f.getAnnotation(Transient.class) != null) {
                JpaColumn col = new JpaColumn(f, this);
                columns.add(col);
            }
        }
    }

    public HashMap<String, JqlSchemaJoin> getTableJoinMap(ArrayList<JpaColumn> columns) {
        HashMap<String, JqlSchemaJoin> tableJoinMap = new HashMap<>();
        for (JpaColumn ci: columns) {
            JqlColumnJoin join = ci.getJoinedForeignKey();
            if (join != null) {
                String joinFieldName = join.getJoinedFieldName();
                JqlSchemaJoin foreignKeys = tableJoinMap.get(joinFieldName);
                if (foreignKeys == null) {
                    foreignKeys = new JqlSchemaJoin(this.getSchemaLoader());
                    tableJoinMap.put(joinFieldName, foreignKeys);
                }
                foreignKeys.add(join);
            }
        }
        return tableJoinMap;
    }
}
