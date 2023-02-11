package org.eipgrid.jql.jdbc.timescale;

import org.eipgrid.jql.JqlStorage;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;

public abstract class TSDBRepositoryBase<ENTITY, ID> extends JPARepositoryBase<ENTITY, ID> {

    private final String timeKeyColumnName;

    public TSDBRepositoryBase(JqlStorage storage, Class<ENTITY> entityType, Class<ID> idType, String timeKeyColumnName) {
        super(storage, entityType);
        this.timeKeyColumnName = timeKeyColumnName;

        QSchema schema = getStorage().loadSchema(getEntityType());
        try {
            new Initializer(schema).initializeTSDB(schema);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTimeKeyColumnName() {
        return timeKeyColumnName;
    }

    private class Initializer extends TSDBHelper {
        private final QSchema schema;

        public Initializer(QSchema schema) {
            super(getStorage(), getTableName());
            this.schema = schema;
        }

        public HashMap<String, AggregateType> resolveAggregationTypeMap() {
            HashMap<String, AggregateType> aggTypeMap = new HashMap<>();
            for (QColumn col : schema.getWritableColumns()) {
                AggregateType aggType = resolveAggregationType(col);
                String col_name = col.getStoredName();
                aggTypeMap.put(col_name, aggType);
            }
            return aggTypeMap;
        }

        private AggregateType resolveAggregationType(QColumn col) {
            Class entityType = getEntityType();
            Field f = ClassUtils.findDeclaredField(entityType, col.getJsonKey());
            if (f == null) {
                return AggregateType.None;
            }
            Aggregate c = f.getAnnotation(Aggregate.class);
            if (c != null) {
                return c.value();
            }
            if (col.getStoredType() == Float.class || col.getStoredType() == Field.class) {
                return AggregateType.Mean;
            }
            return AggregateType.None;
        }

        @Override
        protected String generateDDL(String tableName) {
            throw new RuntimeException("must not reach here!");
        }
    }
}