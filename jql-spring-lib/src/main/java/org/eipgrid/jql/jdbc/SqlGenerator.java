package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JQColumn;
import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.JQJoin;
import org.eipgrid.jql.parser.Expression;
import org.eipgrid.jql.parser.JqlQuery;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.util.SourceWriter;
import org.eipgrid.jql.JQSelect;
import org.springframework.data.domain.Sort;

import java.util.*;

public class SqlGenerator extends SqlConverter implements QueryBuilder {

    public SqlGenerator() {
        super(new SourceWriter('\''));
    }

//    public SqlGenerator(SourceWriter out) {
//        this.sw = sqlConverter;//new SqlWriter(schema, null);
//    }

    protected String getCommand(SqlConverter.Command command) {
        return command.toString();
    }

    protected void writeFilter(JqlFilter jql) {
        super.visitNode(jql);
        Expression ps = jql.getPredicates();
        if (!ps.isEmpty()) {
            ps.accept(this);
            sw.write(" AND ");
        }
        for (JqlFilter child : jql.getChildNodes()) {
            if (!child.isEmpty()) {
                writeFilter(child);
            }
        }
    }

    protected void writeWhere(JqlQuery where) {
        if (!where.isEmpty()) {
            sw.write("\nWHERE ");
            writeFilter(where);
            if (sw.endsWith(" AND ")) {
                sw.shrinkLength(5);
            }
        }
    }

    private void writeFrom(JqlQuery where) {
        writeFrom(where, where.getTableName(), false);
    }

    private void writeFrom(JqlQuery where, String tableName, boolean ignoreEmptyFilter) {
        sw.write("FROM ").write(tableName).write(" as ").write(where.getMappingAlias());
        for (JQResultMapping fetch : where.getResultMappings()) {
            JQJoin join = fetch.getEntityJoin();
            if (join == null) continue;

            if (ignoreEmptyFilter && fetch.isEmpty()) continue;

            String parentAlias = fetch.getParentNode().getMappingAlias();
            String alias = fetch.getMappingAlias();
            if (true || join.isUniqueJoin()) {
                JQJoin associated = join.getAssociativeJoin();
                writeJoinStatement(join, parentAlias, associated == null ? alias : "p" + alias);
                if (associated != null) {
                    writeJoinStatement(associated, "p" + alias, alias);
                }
            } else {

            }
        }
    }


    private void writeJoinStatement(JQJoin joinKeys, String baseAlias, String alias) {
        boolean isInverseMapped = joinKeys.isInverseMapped();
        String joinedTable = joinKeys.getJoinedSchema().getTableName();
        sw.write("\nleft join ").write(joinedTable).write(" as ").write(alias).write(" on\n\t");
        for (JQColumn fk : joinKeys.getForeignKeyColumns()) {
            JQColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            sw.write(baseAlias).write(".").write(anchor.getColumnName());
            sw.write(" = ").write(alias).write(".").write(linked.getColumnName()).write(" and\n\t");
        }
        sw.shrinkLength(6);
    }

    public String createCountQuery(JqlQuery where) {
        sw.write("\nSELECT count(*) ");
        writeFrom(where);
        writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    private boolean needDistinctPagination(JqlQuery where) {
        if (!where.hasArrayDescendantNode()) return false;

        for (JQResultMapping mapping : where.getResultMappings()) {
            JQJoin join = mapping.getEntityJoin();
            if (join == null) continue;

            if (mapping.getSelectedColumns().size() == 0) continue;

            if (mapping.isArrayNode()) {// && mapping.hasFilterPredicates()) {
                return true;
            }
        }
        return false;
    }

    public String createSelectQuery(JqlQuery where, JQSelect columns) {
        sw.reset();
        where.setSelectedProperties(columns.getPropertyKeys());
        String tableName = where.getTableName();

        boolean need_complex_pagination = (columns.getLimit() > 0 || columns.getOffset() > 0) && needDistinctPagination(where);
        if (need_complex_pagination) {
            sw.write("\nWITH _cte AS (\n"); // WITH _cte AS NOT MATERIALIZED
            sw.incTab();
            sw.write("SELECT DISTINCT t_0.* ");
            writeFrom(where, tableName, true);
            writeWhere(where);
            tableName = "_cte";
            writeOrderBy(where, columns.getSort(), false);
            writePagination(columns);
            sw.decTab();
            sw.write("\n)");
        }

        sw.write("\nSELECT\n");
        for (JQResultMapping mapping : where.getResultMappings()) {
            sw.write('\t');
            String alias = mapping.getMappingAlias();
            for (JQColumn col : mapping.getSelectedColumns()) {
                sw.write(alias).write('.').write(col.getColumnName()).write(", ");
            }
            sw.write('\n');
        }
        sw.replaceTrailingComma("\n");
        writeFrom(where, tableName, false);
        writeWhere(where);
        writeOrderBy(where, columns.getSort(), where.hasArrayDescendantNode());
        if (!need_complex_pagination) {
            writePagination(columns);
        }
        String sql = sw.reset();
        return sql;
    }

    private void writeOrderBy(JqlQuery where, Sort sort, boolean need_joined_result_set_ordering) {
        if (!need_joined_result_set_ordering) {
            if (sort == null || sort.isUnsorted()) return;
        }

        sw.write("\nORDER BY ");
        final HashSet<String> explicitSortColumns = new HashSet<>();
        if (sort != null) {
            JQSchema schema = where.getSchema();
            sort.forEach(order -> {
                String p = order.getProperty();
                String qname = where.getMappingAlias() + '.' + schema.getColumn(p).getColumnName();
                explicitSortColumns.add(qname);
                sw.write(qname);
                sw.write(order.isAscending() ? " asc" : " desc").write(", ");
            });
        }
        if (need_joined_result_set_ordering) {
            for (JQResultMapping mapping : where.getResultMappings()) {
                if (!mapping.hasArrayDescendantNode()) continue;
                if (mapping != where && !mapping.isArrayNode()) continue;
                String table = mapping.getMappingAlias();
                for (JQColumn column : mapping.getSchema().getPKColumns()) {
                    String qname = table + '.' + column.getColumnName();
                    if (!explicitSortColumns.contains(qname)) {
                        sw.write(table).write('.').write(column.getColumnName()).write(", ");
                    }
                }
            }
        }
        sw.replaceTrailingComma("");
    }

    private void writePagination(JQSelect pagination) {
        int offset = pagination.getOffset();
        int limit  = pagination.getLimit();
        if (offset > 0) sw.write("\nOFFSET " + offset);
        if (limit > 0) sw.write("\nLIMIT " + limit);
    }

    public String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet) {
        sw.write("\nUPDATE ").write(where.getTableName()).write(" ").write(where.getMappingAlias()).writeln(" SET");

        for (Map.Entry<String, Object> entry : updateSet.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sw.write("  ");
            sw.write(key).write(" = ").writeValue(value);
            sw.write(",\n");
        }
        sw.replaceTrailingComma("\n");
        this.writeWhere(where);
        String sql = sw.reset();
        return sql;

    }

    public String createDeleteQuery(JqlQuery where) {
        sw.write("\nDELETE ");
        this.writeFrom(where);
        this.writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    public String prepareFindByIdStatement(JQSchema schema) {
        sw.write("\nSELECT * FROM ").write(schema.getTableName()).write("\nWHERE ");
        List<JQColumn> keys = schema.getPKColumns();
        for (int i = 0; i < keys.size(); ) {
            String key = keys.get(i).getColumnName();
            sw.write(key).write(" = ? ");
            if (++ i < keys.size()) {
                sw.write(" AND ");
            }
        }
        String sql = sw.reset();
        return sql;
    }

    public String createInsertStatement(JQSchema schema, Map entity, boolean ignoreConflict) {

        Set<String> keys = ((Map<String, ?>)entity).keySet();
        sw.writeln();
        sw.write(getCommand(SqlConverter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
        sw.incTab();
        for (String name : schema.getPhysicalColumnNames(keys)) {
            sw.write(name);
            sw.write(", ");
        }
        sw.shrinkLength(2);
        sw.decTab();
        sw.writeln("\n) VALUES (");
        for (String k : keys) {
            Object v = entity.get(k);
            sw.writeValue(v).write(", ");
        }
        sw.replaceTrailingComma(")");
        if (ignoreConflict) {
            sw.write("\nON CONFLICT DO NOTHING");
        }
        String sql = sw.reset();
        return sql;
    }

    public String prepareBatchInsertStatement(JQSchema schema, boolean ignoreConflict) {
        sw.writeln();
        sw.write(getCommand(SqlConverter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
        for (JQColumn col : schema.getWritableColumns()) {
            sw.write(col.getColumnName()).write(", ");
        }
        sw.replaceTrailingComma("\n) VALUES (");
        for (int i = schema.getWritableColumns().size(); --i >= 0; ) {
            sw.write("?,");
        }
        sw.replaceTrailingComma(")");
        if (ignoreConflict) {
            sw.write("\nON CONFLICT DO NOTHING");
        }
        String sql = sw.reset();
        return sql;
    }

//    public BatchUpsert prepareInsert(JQSchema schema, Collection<Map<String, Object>> entities) {
//        return prepareInsert(schema, entities, schema.getTableName(), true);
//    }
//
//
//    public BatchUpsert prepareInsert(JQSchema schema, Collection<Map<String, Object>> entities, String extendedTableName, boolean ignoreConflict) {
//        String sql = prepareBatchInsertStatement(schema, ignoreConflict);
//        return new BatchUpsert(entities, schema, sql);
//    }


}
