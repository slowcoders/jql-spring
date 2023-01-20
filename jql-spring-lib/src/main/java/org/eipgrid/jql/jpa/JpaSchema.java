package org.eipgrid.jql.jpa;

import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.schema.SchemaLoader;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class JpaSchema extends QSchema {
    public JpaSchema(SchemaLoader schemaLoader, String tableName, Class<?> ormType) {
        super(schemaLoader, tableName, ormType.getTypeName());

        ArrayList<JpaColumn> columns = new ArrayList<>();
        this.createColumns(columns, ormType);
        this.init(columns, ormType);
    }

    private void createColumns(ArrayList<JpaColumn> columns, Class<?> ormType) {
        Class<?> superClass = ormType.getSuperclass();
        if (superClass != Object.class) {
            createColumns(columns, superClass);
        }
        for (Field f : ormType.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.TRANSIENT) == 0 &&
                    f.getAnnotation(Transient.class) != null) {
                JpaColumn col = new JpaColumn(f, this);
                columns.add(col);
            }
        }
    }

//    public HashMap<String, JQJoin> getTableJoinMap(ArrayList<JpaColumn> columns) {
//        HashMap<String, JQJoin> tableJoinMap = new HashMap<>();
//        for (JpaColumn ci: columns) {
//            JqlColumnJoin join = ci.getJoinedForeignKey();
//            if (join != null) {
//                String joinFieldName = join.getJoinedFieldName();
//                JQJoin foreignKeys = tableJoinMap.get(joinFieldName);
//                if (foreignKeys == null) {
//                    foreignKeys = new JQJoin(this.getSchemaLoader());
//                    tableJoinMap.put(joinFieldName, foreignKeys);
//                }
//                foreignKeys.add(join);
//            }
//        }
//        return tableJoinMap;
//    }
}
