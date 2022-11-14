package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.jdbc.metadata.JqlRowMapper;
import org.slowcoders.jql.parser.JqlQuery;
import org.slowcoders.jql.parser.QueryBuilder;
import org.slowcoders.jql.parser.SourceWriter;
import org.slowcoders.jql.parser.SqlBuilder;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class SearchQuery {
    String query;
    JqlQuery where;
    JdbcTemplate jdbc;
    public SearchQuery(JqlQuery where, QueryBuilder builder) {
        this.where = where;
        this.query = builder.createSelectQuery(where);
    }


    public List<KVEntity> execute(JdbcTemplate jdbc, Sort sort, int limit, int offset) {
        SourceWriter sb = new SourceWriter('\'');
        sb.write(query);

        write_orderBy(sb, sort);

        if (offset > 0) sb.write("\nOFFSET " + limit);

        if (limit > 0) sb.write("\nLIMIT " + limit);

        JqlRowMapper rowMapper = new JqlRowMapper(where.getResultMappings());
        return (List)jdbc.query(query, rowMapper);
    }

    private void write_orderBy(SourceWriter sb, Sort sort) {
        if (sort == null) return;

        sb.write("\nORDER BY ");
        sort.forEach(order -> {
            String p = order.getProperty();
            sb.write(where.getSchema().getColumn(p).getColumnName());
            sb.write(order.isAscending() ? " asc" : " desc").write(", ");
        });
        sb.replaceTrailingComma("\n");
    }

}
