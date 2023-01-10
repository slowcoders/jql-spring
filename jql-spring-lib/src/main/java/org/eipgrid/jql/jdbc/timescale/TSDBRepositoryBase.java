package org.eipgrid.jql.jdbc.timescale;

import org.eipgrid.jql.JQColumn;
import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.JQType;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.spring.JQService;
import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;

public abstract class TSDBRepositoryBase<ENTITY, ID> extends JPARepositoryBase<ENTITY, ID> {

    private final String timeKeyColumnName;

    public TSDBRepositoryBase(JQService service, String timeKeyColumnName) {
        super(service);
        this.timeKeyColumnName = timeKeyColumnName;

        JQSchema schema = getService().loadSchema(getEntityType());
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
        private final JQSchema schema;

        public Initializer(JQSchema schema) {
            super(getService(), getTableName());
            this.schema = schema;
        }

        public HashMap<String, AggregateType> resolveAggregationTypeMap() {
            HashMap<String, AggregateType> aggTypeMap = new HashMap<>();
            for (JQColumn col : schema.getWritableColumns()) {
                AggregateType aggType = resolveAggregationType(col);
                String col_name = col.getPhysicalName();
                aggTypeMap.put(col_name, aggType);
            }
            return aggTypeMap;
        }

        private AggregateType resolveAggregationType(JQColumn col) {
            Class entityType = getEntityType();
            Field f = ClassUtils.findDeclaredField(entityType, col.getJsonKey());
            if (f == null) {
                return AggregateType.None;
            }
            Aggregate c = f.getAnnotation(Aggregate.class);
            if (c != null) {
                return c.value();
            }
            if (col.getType() == JQType.Float) {
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