package org.eipgrid.jql.jpa;

import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.SchemaLoader;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class JpaSchema extends JqlSchema {
    public JpaSchema(SchemaLoader schemaLoader, String tableName, Class<?> entityType) {
        super(schemaLoader, tableName, entityType.getTypeName());

        ArrayList<JpaColumn> columns = new ArrayList<>();
        this.createColumns(columns, entityType);
        this.init(columns);
    }

    private void createColumns(ArrayList<JpaColumn> columns, Class<?> entityType) {
        Class<?> superClass = entityType.getSuperclass();
        if (superClass != Object.class) {
            createColumns(columns, superClass);
        }
        for (Field f : entityType.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.TRANSIENT) == 0 &&
                    f.getAnnotation(Transient.class) != null) {
                JpaColumn col = new JpaColumn(f, this);
                columns.add(col);
            }
        }
    }

//    public HashMap<String, JqlSchemaJoin> getTableJoinMap(ArrayList<JpaColumn> columns) {
//        HashMap<String, JqlSchemaJoin> tableJoinMap = new HashMap<>();
//        for (JpaColumn ci: columns) {
//            JqlColumnJoin join = ci.getJoinedForeignKey();
//            if (join != null) {
//                String joinFieldName = join.getJoinedFieldName();
//                JqlSchemaJoin foreignKeys = tableJoinMap.get(joinFieldName);
//                if (foreignKeys == null) {
//                    foreignKeys = new JqlSchemaJoin(this.getSchemaLoader());
//                    tableJoinMap.put(joinFieldName, foreignKeys);
//                }
//                foreignKeys.add(join);
//            }
//        }
//        return tableJoinMap;
//    }
}
