package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.parser.*;
import org.springframework.data.domain.Sort;

import java.util.*;

public class SqlGenerator implements QueryBuilder {

    private final SqlWriter sw;

    public SqlGenerator(JqlSchema schema) {
        this(new SqlWriter(schema));
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
        sw.write("FROM ").write(where.getSchema().getTableName());
        for (JqlResultMapping fetch : where.getResultColumnMappings()) {
            JqlEntityJoin join = fetch.getEntityJoin();
            if (join == null) continue;

            if (true || join.isUniqueJoin()) {
                writeJoinStatement(join);
                join = join.getAssociativeJoin();
                if (join != null) {
                    writeJoinStatement(join);
                }
            } else {

            }
        }
    }


    private void writeJoinStatement(JqlEntityJoin joinKeys) {
        boolean isInverseMapped = joinKeys.isInverseMapped();
        String joinedTable = joinKeys.getJoinedSchema().getTableName();
        sw.write("\nleft outer join ").write(joinedTable).write(" on\n\t");
        for (JqlColumn fk : joinKeys.getForeignKeyColumns()) {
            JqlColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            sw.write(joinKeys.getBaseSchema().getTableName()).write(".").write(anchor.getColumnName());
            sw.write(" = ").write(linked.getSchema().getTableName()).write(".")
                    .write(linked.getColumnName()).write(" and\n\t");
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
        sw.write("\nSELECT ");

        for (JqlResultMapping fetch : where.getResultColumnMappings()) {
            String table = fetch.getSchema().getTableName();
            List<JqlColumn> selectColumns = fetch.getSelectColumns();
            if (selectColumns == null) {
                sw.write(table).write(".*, ");
            }
            else {
                for (JqlColumn col : selectColumns) {
                    sw.write(table).write('.').write(col.getColumnName()).
                            write(" as ").write('\"').write(col.getJsonKey()).write("\",\n");
                }
            }
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
            sw.writeEquals(key, value);
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
        sw.writeColumnNames(schema.getPhysicalColumnNames(keys), false);
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
        for (JqlColumn col : schema.getWritableColumns()) {
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
