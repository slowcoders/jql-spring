package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;
import org.slowcoders.jql.parser.JqlParser;
import org.slowcoders.jql.parser.JqlQuery;
import org.slowcoders.jql.parser.SQLWriter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class JDBCQueryBuilder {

    private JqlSchema jqlSchema;
    private ConversionService conversionService;

    public JDBCQueryBuilder(ConversionService conversionService, JqlSchema jqlSchema) {
        this.jqlSchema = jqlSchema;
        this.conversionService = conversionService;
    }

    protected String buildSearchQuery(Object filter, Sort sort, int limit, int offset) {

        JqlQuery where = build_where(filter, true);
        StringBuilder order_by = new StringBuilder("\n");
        if (sort != null) {
            order_by.append("ORDER BY ");
            sort.forEach(order -> {
                order_by.append(order.getProperty());
                order_by.append(order.isAscending() ? " asc" : " desc").append(", ");
            });
        }
        if (order_by.length() > 2) {
            order_by.setLength(order_by.length() - 2);
        }

        SQLWriter sb = new SQLWriter(jqlSchema);
        where.writeSelect(sb);
        sb.write("FROM ").writeWhere(where, true);
        sb.write(order_by.toString());
        if (offset > 0) {
            sb.write("\nOFFSET " + limit);
        }
        if (limit > 0) {
            sb.write("\nLIMIT " + limit);
        }
        String sql = sb.toString();
        System.out.println(sql);
        return sql;
    }

    protected String buildCountQuery(Map<String, Object> filter) {
        JqlQuery where = build_where(filter, false);
        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write("SELECT count(*) FROM ").writeWhere(where, true);
        return sb.toString();
    }


    protected String buildUpdateQuery(Object filter, Map<String, Object> updateSet) {
        JqlQuery where = build_where(filter, false);
        if (where.isEmpty()) {
            throw new RuntimeException("Update All is not permitted");
        }

        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write("UPDATE ").writeTableName().write(" SET\n");
        for (Map.Entry<String, Object> entry : updateSet.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sb.write("  ").writeEquals(key, value).write(",\n");
        }
        sb.replaceTrailingComma("\n");
        sb.writeWhere(where, false);
        String sql = sb.toString();
        return sql;
    }

    protected String buildDeleteQuery(Object filter) {
        JqlQuery where = build_where(filter, false);
        if (where.isEmpty()) {
            throw new RuntimeException("Delete All is not permitted");
        }

        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write("DELETE FROM ").writeTableName().writeWhere(where, false);
        return sb.toString();
    }


    public <T> T convert(Object arg, Class<T> type) {
        return conversionService.convert(arg, type);
    }

    private static final Map _emptyMap = new HashMap();
    private JqlQuery build_where(Object filter, boolean fetchData) {
        Map filterMap;
        if (filter instanceof Map) {
            filterMap = (Map) filter;
        }
        else if (filter != null) {
            filterMap = new HashMap();
            throw new RuntimeException("Not implemented");
            //filterMap.put(this.pkColName + "@eq", filter);
            /** 참고 ID 검색 함수를 아래와 같이 처리할 수도 있다. (단, FetchType.EAGER 로 인한 다중 query 발생)
                Expression<?> column = root.get(this.pkColName).as(this.idType);
                return JQLOperator.EQ.createPredicate(cb, column, filter);
            */
        }
        else {
            /**
             * 참고) 왜 불필요하게 JQLParser 를 수행하는가?
             * CriteriaQuery 는 FetchType.EAGER 가 지정된 Column 에 대해
             * 자동으로 fetch 하는 기능이 없다. (2022.02.18 현재) 이에 대량 쿼리 발생시
             * 그 개수만큼 Join 된 칼럼을 읽어오는 추가적인 쿼리가 발생한다.
             * Parser 는 내부에서 이를 별도 검사하여 처리한다.
             */
            filterMap = _emptyMap;
        }

        JqlQuery query = new JqlQuery(this.jqlSchema);
        JqlParser parser = new JqlParser(query, conversionService);
        parser.parse(query, filterMap);
        return query;
    }


    protected String build_findById(Object id) {
        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write("SELECT * FROM ").writeTableName().write("\nWHERE ");
        List<JqlColumn> keys = jqlSchema.getPKColumns();
        if (keys.size() == 0) {
            sb.writeEquals(keys.get(0).getColumnName(), id);
        }
        else {
            Object[] values = (Object[]) id;
            for (int i = 0; i < keys.size(); ) {
                String key = keys.get(i).getColumnName();
                sb.writeEquals(key, values[i]);
                if (++ i < keys.size()) {
                    sb.write(" AND ");
                }
            }
        }
        return sb.toString();
    }

    protected String getCommand(Command command) {
        return command.toString();
    }

    public String build_insert(Map entity, boolean ignoreConflict) {
        Set<String> keys = ((Map<String, ?>)entity).keySet();
        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write(getCommand(Command.Insert)).write(" INTO ").writeTableName().write("(");
        sb.writeColumnNames(jqlSchema.getPhysicalColumnNames(keys), false);
        sb.write(") VALUES ");
        sb.write("(");
        for (String k : keys) {
            Object v = entity.get(k);
            sb.writeValue(v).write(", ");
        }
        sb.replaceTrailingComma(")");
        if (ignoreConflict) {
            sb.write(" ON CONFLICT DO NOTHING");
        }
        return sb.toString();
    }

    public String build_insert(Iterable<Map<String, Object>> entities, boolean ignoreConflict) {
        return build_insert_ex(entities, jqlSchema.getTableName(), ignoreConflict);
    }

    public String build_insert_ex(Iterable<Map<String, Object>> entities, String tableName, boolean ignoreConflict) {
        Iterator<Map<String, Object>> it = entities.iterator();
        Map<String, Object> first = it.next();
        Set<String> keys = ((Map<String, ?>)first).keySet();
        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write(getCommand(Command.Insert)).write(" INTO ").write(tableName).write("(");
        sb.writeColumnNames(jqlSchema.getPhysicalColumnNames(keys), false);
        sb.write(") VALUES ");
        for (Map<String, Object> entity = first; entity != null; entity = it.hasNext() ? it.next() : null) {
            sb.write("(");
            for (String k : keys) {
                Object v = entity.get(k);
                sb.writeValue(v).write(", ");
            }
            sb.replaceTrailingComma("),\n");
        }
        if (ignoreConflict) {
            sb.replaceTrailingComma("\nON CONFLICT DO NOTHING");
        }
        else {
            sb.replaceTrailingComma("");
        }
        return sb.toString();
    }

    public JqlSchema getSchema() {
        return jqlSchema;
    }

    public String prepareBatchInsert() {
        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write(getCommand(Command.Insert)).write(" INTO ").writeTableName().write("(");
        for (JqlColumn col : jqlSchema.getWritableColumns()) {
            sb.write(col.getColumnName()).write(", ");
        }
        sb.replaceTrailingComma(")");
        return sb.toString();
    }

    protected BatchUpsert prepareInsert(Collection<Map<String, Object>> entities) {
        return prepareInsert(entities, getSchema().getTableName(), true);
    }

    protected BatchUpsert prepareInsert(Collection<Map<String, Object>> entities, String extendedTableName, boolean ignoreConflict) {
        SQLWriter sb = new SQLWriter(jqlSchema);
        sb.write(getCommand(Command.Insert)).write(" INTO ").write(extendedTableName).write("(");
        for (JqlColumn col : getSchema().getWritableColumns()) {
            sb.write(col.getColumnName()).write(", ");
        }
        sb.replaceTrailingComma(") VALUES (");
        for (JqlColumn col : getSchema().getWritableColumns()) {
            sb.write("?,");
        }
        sb.replaceTrailingComma(")");
        if (ignoreConflict) {
            sb.write("\nON CONFLICT DO NOTHING");
        }
        return new BatchUpsert(entities, this.getSchema(), sb.toString());
    }


    public static class BatchUpsert<ID> implements BatchPreparedStatementSetterWithKeyHolder {
        private final Map<String, Object>[] entities;
        private final List<JqlColumn> columns;
        private final String sql;
        private final JqlSchema schema;
        private List<Map<String, Object>> generatedKeys;

        BatchUpsert(Collection<Map<String, Object>> entities, JqlSchema schema, String sql) {
            this.schema = schema;
            this.columns = schema.getWritableColumns();
            this.entities = entities.toArray(new Map[entities.size()]);
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Map<String, Object> entity = entities[i];
            int idx = 0;
            for (JqlColumn col : columns) {
                Object json_v = entity.get(col.getJsonName());
                Object value = convertJsonValueToColumnValue(col, json_v);
                ps.setObject(++idx, value);
            }
            ps.getGeneratedKeys();
        }

        private Object convertJsonValueToColumnValue(JqlColumn col, Object v) {
            if (v == null) return null;

            if (v.getClass().isEnum()) {
                if (col.getValueFormat() == JsonNodeType.Text) {
                    return v.toString();
                }
                else {
                    return ((Enum)v).ordinal();
                }
            }
            return v;
        }


        @Override
        public int getBatchSize() {
            return entities.length;
        }

        @Override
        public void setGeneratedKeys(List<Map<String, Object>> keys) {
            this.generatedKeys = keys;
        }

        public List<ID> getEntityIDs() {
            int cntKeys = generatedKeys == null ? 0 : generatedKeys.size();
            ArrayList<ID> ids = new ArrayList<>();
            for (int i = 0; i < entities.length; i ++) {
                ID id = (ID)schema.extractEntityId(entities[i], i < cntKeys ? this.generatedKeys.get(i) : null);
                ids.add(id);
            }
            return ids;
        }
    }

}