package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.metadata.JqlRowMapper;
import org.slowcoders.jql.parser.JqlQuery;
import org.slowcoders.jql.parser.QueryBuilder;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class SearchQuery {
    String query;
    JqlQuery where;
    JdbcTemplate jdbc;
    public SearchQuery(JqlQuery where) {
        this.where = where;

        QueryBuilder sb = new QueryBuilder(where.getSchema());
        sb.write("\nSELECT ");
        if (true) {
            for (JqlQuery.FetchInfo fetch : where.getFetchList()) {
                JqlSchema table = fetch.schema;
                sb.write(table.getTableName()).write(".*, ");
            }
        } else {
            for (JqlQuery.FetchInfo fetch : where.getFetchList()) {
                JqlSchema table = fetch.schema;
                for (JqlColumn col : table.getReadableColumns()) {
                    sb.write(table.getTableName()).write('.').write(col.getColumnName()).
                            write(" as ").write('\"').write(col.getJsonName()).write("\",\n");
                }
            }
        }
        sb.replaceTrailingComma("\nFROM ");
        sb.writeWhere(where, true);
        query = sb.toString();
    }


    public List<KVEntity> execute(JdbcTemplate jdbc, Sort sort, int limit, int offset) {
        QueryBuilder sb = new QueryBuilder(where.getSchema());
        sb.write(query);

        write_orderBy(sb, sort);

        if (offset > 0) sb.write("\nOFFSET " + limit);

        if (limit > 0) sb.write("\nLIMIT " + limit);

        JqlRowMapper rowMapper = new JqlRowMapper(where.getFetchList());
        return (List)jdbc.query(query, rowMapper);
    }

    private void write_orderBy(QueryBuilder sb, Sort sort) {
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
