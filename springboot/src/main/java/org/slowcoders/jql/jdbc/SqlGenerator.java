package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.parser.JqlParser;
import org.slowcoders.jql.parser.JqlQuery;
import org.slowcoders.jql.parser.QueryBuilder;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;

import java.util.*;

public class SqlGenerator { //extends QueryBuilder {

    private JqlSchema jqlSchema;
    private ConversionService conversionService;

    public SqlGenerator(ConversionService conversionService, JqlSchema jqlSchema) {
        this.jqlSchema = jqlSchema;
        this.conversionService = conversionService;
    }

    
    public SearchQuery select(Map<String, Object> filter) {

        JqlQuery where = build_where(filter, true);
        return new SearchQuery(where);
//        QueryBuilder sb = new QueryBuilder(jqlSchema);
//        sb.write("\nSELECT ");
//        if (false) {
//            for (JqlSchema table : where.getFetchTables()) {
//                sb.write(table.getTableName()).write(".*, ");
//            }
//        } else {
//            for (JqlSchema table : where.getFetchTables()) {
//                for (JqlColumn col : table.getReadableColumns()) {
//                    sb.write(table.getTableName()).write('.').write(col.getColumnName()).
//                            write(" as ").write('\"').write(col.getJsonName()).write("\",\n");
//                }
//            }
//        }
//        sb.replaceTrailingComma("\nFROM ").writeWhere(where, true);
//
//        write_orderBy(sb, sort);
//
//        if (offset > 0) sb.write("\nOFFSET " + limit);
//
//        if (limit > 0) sb.write("\nLIMIT " + limit);
//
//        String sql = sb.toString();
//        return sql;
    }


    public String count(Map<String, Object> filter) {
        JqlQuery where = build_where(filter, false);
        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.write("\nSELECT count(*) FROM ").writeWhere(where, true);
        return sb.toString();
    }


    
    public String update(Map<String, Object> filter, Map<String, Object> updateSet) {
        JqlQuery where = build_where(filter, false);
        if (where.isEmpty()) {
            throw new RuntimeException("Update All is not permitted");
        }

        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.write("\nUPDATE ").writeTableName().write(" SET\n");
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

    
    public String delete(Map<String, Object> filter) {
        JqlQuery where = build_where(filter, false);
        if (where.isEmpty()) {
            throw new RuntimeException("Delete All is not permitted");
        }

        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.write("\nDELETE FROM ").writeTableName().writeWhere(where, false);
        return sb.toString();
    }


    public <T> T convert(Object arg, Class<T> type) {
        return conversionService.convert(arg, type);
    }

    private static final Map _emptyMap = new HashMap();
    private JqlQuery build_where(Map<String, Object> filter, boolean fetchData) {
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
        else if (!fetchData) {
            return new JqlQuery(this.jqlSchema);
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

        JqlParser parser = new JqlParser(this.jqlSchema, conversionService);
        JqlQuery where = parser.parse(filterMap);
        return where;
    }


    
    public String findById(Object id) {
        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.write("\nSELECT * FROM ").writeTableName().write("\nWHERE ");
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

    
    public String insert(Map entity, boolean ignoreConflict) {
        Set<String> keys = ((Map<String, ?>)entity).keySet();
        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.writeln();
        sb.write(getCommand(Command.Insert)).write(" INTO ").writeTableName().writeln("(");
        sb.incTab();
        sb.writeColumnNames(jqlSchema.getPhysicalColumnNames(keys), false);
        sb.decTab();
        sb.writeln("\n) VALUES (");
        for (String k : keys) {
            Object v = entity.get(k);
            sb.writeValue(v).write(", ");
        }
        sb.replaceTrailingComma(")");
        if (ignoreConflict) {
            sb.write("\nON CONFLICT DO NOTHING");
        }
        return sb.toString();
    }

    
    public String insert(Iterable<Map<String, Object>> entities, boolean ignoreConflict) {
        return insert_ex(entities, jqlSchema.getTableName(), ignoreConflict);
    }

    
    public String insert_ex(Iterable<Map<String, Object>> entities, String tableName, boolean ignoreConflict) {
        Iterator<Map<String, Object>> it = entities.iterator();
        Map<String, Object> first = it.next();
        Set<String> keys = ((Map<String, ?>)first).keySet();
        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.writeln();
        sb.write(getCommand(Command.Insert)).write(" INTO ").write(tableName).writeln("(");
        sb.incTab();
        sb.writeColumnNames(jqlSchema.getPhysicalColumnNames(keys), false);
        sb.decTab();
        sb.write("\n) VALUES ");
        for (Map<String, Object> entity = first; entity != null; entity = it.hasNext() ? it.next() : null) {
            sb.write("(\n");
            sb.incTab();
            for (String k : keys) {
                Object v = entity.get(k);
                sb.writeValue(v).write(", ");
            }
            sb.decTab();
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

    
    public BatchUpsert prepareInsert(Collection<Map<String, Object>> entities) {
        return prepareInsert(entities, getSchema().getTableName(), true);
    }

    
    public BatchUpsert prepareInsert(Collection<Map<String, Object>> entities, String extendedTableName, boolean ignoreConflict) {
        QueryBuilder sb = new QueryBuilder(jqlSchema);
        sb.writeln();
        sb.write(getCommand(Command.Insert)).write(" INTO ").write(extendedTableName).writeln("(");
        for (JqlColumn col : getSchema().getWritableColumns()) {
            sb.write(col.getColumnName()).write(", ");
        }
        sb.replaceTrailingComma("\n) VALUES (");
        for (JqlColumn col : getSchema().getWritableColumns()) {
            sb.write("?,");
        }
        sb.replaceTrailingComma(")");
        if (ignoreConflict) {
            sb.write("\nON CONFLICT DO NOTHING");
        }
        return new BatchUpsert(entities, this.getSchema(), sb.toString());
    }


}