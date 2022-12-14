package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.parser.AstRoot;
import org.eipgrid.jql.util.SourceWriter;
import org.eipgrid.jql.JqlSelect;
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

    protected void writeWhere(AstRoot where) {
        if (!where.isEmpty()) {
            sw.write("\nWHERE ");
            where.accept(this);
        }
    }

    private void writeFrom(AstRoot where) {
        writeFrom(where, where.getTableName(), false);
    }

    private void writeFrom(AstRoot where, String tableName, boolean ignoreEmptyFilter) {
        sw.write("FROM ").write(tableName).write(" as ").write(where.getMappingAlias());
        for (JqlResultMapping fetch : where.getResultColumnMappings()) {
            JqlSchemaJoin join = fetch.getSchemaJoin();
            if (join == null) continue;

            if (ignoreEmptyFilter && !fetch.hasFilterPredicates()) continue;

            String parentAlias = fetch.getParentNode().getMappingAlias();
            String alias = fetch.getMappingAlias();
            if (true || join.isUniqueJoin()) {
                JqlSchemaJoin associated = join.getAssociativeJoin();
                writeJoinStatement(join, parentAlias, associated == null ? alias : "p" + alias);
                if (associated != null) {
                    writeJoinStatement(associated, "p" + alias, alias);
                }
            } else {

            }
        }
    }


    private void writeJoinStatement(JqlSchemaJoin joinKeys, String baseAlias, String alias) {
        boolean isInverseMapped = joinKeys.isInverseMapped();
        String joinedTable = joinKeys.getJoinedSchema().getTableName();
        sw.write("\ninner join ").write(joinedTable).write(" as ").write(alias).write(" on\n\t");
        for (JqlColumn fk : joinKeys.getForeignKeyColumns()) {
            JqlColumn anchor, linked;
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

    public String createCountQuery(AstRoot where) {
        sw.write("\nSELECT count(*) ");
        writeFrom(where);
        writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    private boolean needDistinctPagination(AstRoot where) {
        if (where.isLinearNode()) return false;

        for (JqlResultMapping mapping : where.getResultColumnMappings()) {
            JqlSchemaJoin join = mapping.getSchemaJoin();
            if (join == null) continue;

            if (mapping.getSelectedColumns().size() == 0) continue;

            if (mapping.isArrayNode()) {// && mapping.hasFilterPredicates()) {
                return true;
            }
        }
        return false;
    }

    public String createSelectQuery(AstRoot where, JqlSelect columns) {
        sw.reset();
        String tableName = where.getTableName();
        where.setSelectedColumns(columns);

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
        for (JqlResultMapping mapping : where.getResultColumnMappings()) {
            sw.write('\t');
            String alias = mapping.getMappingAlias();
            for (JqlColumn col : mapping.getSelectedColumns()) {
                sw.write(alias).write('.').write(col.getColumnName()).write(", ");
            }
            sw.write('\n');
        }
        sw.replaceTrailingComma("\n");
        writeFrom(where, tableName, false);
        writeWhere(where);
        writeOrderBy(where, columns.getSort(), !where.isLinearNode());
        if (!need_complex_pagination) {
            writePagination(columns);
        }
        String sql = sw.reset();
        return sql;
    }

    private void writeOrderBy(AstRoot where, Sort sort, boolean need_joined_result_set_ordering) {
        if (!need_joined_result_set_ordering) {
            if (sort == null || sort.isUnsorted()) return;
        }

        sw.write("\nORDER BY ");
        final HashSet<String> explicitSortColumns = new HashSet<>();
        if (sort != null) {
            JqlSchema schema = where.getSchema();
            sort.forEach(order -> {
                String p = order.getProperty();
                String qname = where.getMappingAlias() + '.' + schema.getColumn(p).getColumnName();
                explicitSortColumns.add(qname);
                sw.write(qname);
                sw.write(order.isAscending() ? " asc" : " desc").write(", ");
            });
        }
        if (need_joined_result_set_ordering) {
            for (JqlResultMapping mapping : where.getResultColumnMappings()) {
                if (mapping.isLinearNode()) continue;
                if (mapping != where && !mapping.isArrayNode()) continue;
                String table = mapping.getMappingAlias();
                for (JqlColumn column : mapping.getSchema().getPKColumns()) {
                    String qname = table + '.' + column.getColumnName();
                    if (!explicitSortColumns.contains(qname)) {
                        sw.write(table).write('.').write(column.getColumnName()).write(", ");
                    }
                }
            }
        }
        sw.replaceTrailingComma("");
    }

    private void writePagination(JqlSelect pagination) {
        int offset = pagination.getOffset();
        int limit  = pagination.getLimit();
        if (offset > 0) sw.write("\nOFFSET " + offset);
        if (limit > 0) sw.write("\nLIMIT " + limit);
    }

    public String createUpdateQuery(AstRoot where, Map<String, Object> updateSet) {
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

    public String createDeleteQuery(AstRoot where) {
        sw.write("\nDELETE ");
        this.writeFrom(where);
        this.writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    public String prepareFindByIdStatement(JqlSchema schema) {
        sw.write("\nSELECT * FROM ").write(schema.getTableName()).write("\nWHERE ");
        List<JqlColumn> keys = schema.getPKColumns();
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

    public String createInsertStatement(JqlSchema schema, Map entity, boolean ignoreConflict) {

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

    public String prepareBatchInsertStatement(JqlSchema schema, boolean ignoreConflict) {
        sw.writeln();
        sw.write(getCommand(SqlConverter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
        for (JqlColumn col : schema.getWritableColumns()) {
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

    public BatchUpsert prepareInsert(JqlSchema schema, Collection<Map<String, Object>> entities) {
        return prepareInsert(schema, entities, schema.getTableName(), true);
    }


    public BatchUpsert prepareInsert(JqlSchema schema, Collection<Map<String, Object>> entities, String extendedTableName, boolean ignoreConflict) {
        String sql = prepareBatchInsertStatement(schema, ignoreConflict);
        return new BatchUpsert(entities, schema, sql);
    }


}
