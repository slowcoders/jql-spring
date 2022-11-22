package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.parser.*;
import org.springframework.data.domain.Sort;

import java.util.*;

public class SqlGenerator implements QueryBuilder {

    private final SqlWriter sw;

    public SqlGenerator() {
        this(new SqlWriter());
    }

    public SqlGenerator(SqlWriter sqlWriter) {
        this.sw = sqlWriter;//new SqlWriter(schema, null);
    }

    protected String getCommand(SqlWriter.Command command) {
        return command.toString();
    }

    protected void writeWhere(JqlQuery where) {
        if (!where.isEmpty()) {
            sw.writeRaw("\nWHERE ");
            where.accept(sw);
        }
    }

    private void writeFrom(JqlQuery where) {
        sw.write("FROM ").write(where.getTableName()).write(" as ").write(where.getMappingAlias());
        for (JqlResultMapping fetch : where.getResultColumnMappings()) {
            JqlSchemaJoin join = fetch.getEntityJoin();
            if (join == null) continue;

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
        sw.write("\nleft outer join ").write(joinedTable).write(" as ").write(alias).write(" on\n\t");
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

    public String createCountQuery(JqlQuery where) {
        sw.write("\nSELECT count(*) ");
        writeFrom(where);
        writeWhere(where);
        String sql = sw.reset();
        return sql;
    }

    public String createSelectQuery(JqlQuery where, Sort sort, int limit, int offset) {
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
        writeFrom(where);
        writeWhere(where);
        write_orderBy(where.getSchema(), sort);
        if (offset > 0) sw.write("\nOFFSET " + limit);
        if (limit > 0) sw.write("\nLIMIT " + limit);

        String sql = sw.reset();
        return sql;
    }

    private void write_orderBy(JqlSchema schema, Sort sort) {
        if (sort == null) return;

        sw.write("\nORDER BY ");
        sort.forEach(order -> {
            String p = order.getProperty();
            sw.write(schema.getColumn(p).getColumnName());
            sw.write(order.isAscending() ? " asc" : " desc").write(", ");
        });
        sw.replaceTrailingComma("\n");
    }


    public String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet) {
        sw.write("\nUPDATE ").write(where.getTableName()).write(" SET\n");

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
        sw.write(getCommand(SqlWriter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
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
        sw.write(getCommand(SqlWriter.Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
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
