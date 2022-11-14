package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.parser.JqlParser;
import org.slowcoders.jql.parser.JqlQuery;
import org.slowcoders.jql.parser.SourceWriter;
import org.slowcoders.jql.parser.SqlBuilder;
import org.springframework.core.convert.ConversionService;

import java.util.*;

public class SqlGenerator extends SqlBuilder {

    private JqlSchema jqlSchema;
    private ConversionService conversionService;

    public SqlGenerator(ConversionService conversionService, JqlSchema jqlSchema) {
        super(jqlSchema);
        this.jqlSchema = jqlSchema;
        this.conversionService = conversionService;
    }

    
    public SearchQuery select(Map<String, Object> filter) {
        JqlQuery where = build_where(filter, true);
        return new SearchQuery(where, this);
    }


    public String count(Map<String, Object> filter) {
        JqlQuery where = build_where(filter, false);
        SqlBuilder sb = new SqlBuilder(jqlSchema);
        String sql = sb.createCountQuery(where);
        return sql;//sb.toString();
    }


    
    public String update(Map<String, Object> filter, Map<String, Object> updateSet) {
        JqlQuery where = build_where(filter, false);
        if (where.isEmpty()) {
            throw new RuntimeException("Update All is not permitted");
        }

        SqlBuilder sb = new SqlBuilder(jqlSchema);
        String sql = sb.createUpdateQuery(where, updateSet);
        return sql;
    }

    
    public String delete(Map<String, Object> filter) {
        JqlQuery where = build_where(filter, false);
        if (where.isEmpty()) {
            throw new RuntimeException("Delete All is not permitted");
        }

        SqlBuilder sb = new SqlBuilder(jqlSchema);
        String sql = sb.createDeleteQuery(where);
        return sql;
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
        SqlBuilder sb = new SqlBuilder(jqlSchema);
        String sql = sb.prepareFindByIdStatement();
        return sql;
    }

    protected String getCommand(Command command) {
        return command.toString();
    }

    
    public String insert(Map entity, boolean ignoreConflict) {
        SqlBuilder sb = new SqlBuilder(jqlSchema);
        String sql = sb.createInsertStatement(entity, ignoreConflict);
        return sql;
    }


    public JqlSchema getSchema() {
        return jqlSchema;
    }

    
    public BatchUpsert prepareInsert(Collection<Map<String, Object>> entities) {
        return prepareInsert(entities, getSchema().getTableName(), true);
    }

    
    public BatchUpsert prepareInsert(Collection<Map<String, Object>> entities, String extendedTableName, boolean ignoreConflict) {
        SqlBuilder sb = new SqlBuilder(jqlSchema);
        String sql = sb.prepareBatchInsertStatement(ignoreConflict);
        return new BatchUpsert(entities, this.getSchema(), sql);
    }


}