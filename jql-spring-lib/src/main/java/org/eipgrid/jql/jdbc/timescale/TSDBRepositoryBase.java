package org.eipgrid.jql.jdbc.timescale;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlValueKind;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.spring.JQLService;
import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.HashMap;

public abstract class TSDBRepositoryBase<ENTITY, ID> extends JPARepositoryBase<ENTITY, ID> {

    private final String timeKeyColumnName;

    public TSDBRepositoryBase(JQLService service, String timeKeyColumnName) {
        super(service);
        this.timeKeyColumnName = timeKeyColumnName;

        JqlSchema schema = getService().loadSchema(getEntityType());
        new Initializer(schema).initializeTSDB(schema);
    }

    public String getTimeKeyColumnName() {
        return timeKeyColumnName;
    }


    private class Initializer extends TSDBHelper {
        private final JqlSchema schema;

        public Initializer(JqlSchema schema) {
            super(getService(), getTableName());
            this.schema = schema;
        }

        public HashMap<String, AggregateType> resolveAggregationTypeMap() {
            HashMap<String, AggregateType> aggTypeMap = new HashMap<>();
            for (JqlColumn col : schema.getWritableColumns()) {
                AggregateType aggType = resolveAggregationType(col);
                String col_name = col.getColumnName();
                aggTypeMap.put(col_name, aggType);
            }
            return aggTypeMap;
        }

        private AggregateType resolveAggregationType(JqlColumn col) {
            Class entityType = getEntityType();
            Field f = ClassUtils.findDeclaredField(entityType, col.getJsonKey());
            if (f == null) {
                return AggregateType.None;
            }
            Aggregate c = f.getAnnotation(Aggregate.class);
            if (c != null) {
                return c.value();
            }
            if (col.getValueKind() == JqlValueKind.Float) {
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