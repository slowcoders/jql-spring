package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.*;

public class SqlBuilder implements JqlPredicateVisitor, JqlEntityJoinVisitor, QueryBuilder {
    private JqlSchema schema;
    private final SourceWriter<SourceWriter> sb = new SourceWriter<SourceWriter>('\'');

    public enum Command {
        Insert,
        Delete,
        Update,
    }

    public SqlBuilder(JqlSchema schema) {
        this.schema = schema;
    }

    public JqlSchema getWorkingSchema() {
        return schema;
    }

    public JqlSchema setWorkingSchema(JqlSchema jqlSchema) {
        JqlSchema old = this.schema;
        this.schema = jqlSchema;
        return old;
    }

    public void writeColumnNames(Iterable<String> names, boolean withTableName) {
        for (String name : names) {
            writeColumnName(name, withTableName);
            sb.write(", ");
        }
        sb.shrinkLength(2);
    }


    @Override
    public void visitCompare(QAttribute column, CompareOperator operator, Object value) {
        column.printSQL(sb);
        String op = "";
        assert (value != null);
        switch (operator) {
            case EQ:
                op = " = ";
                break;
            case NE:
                op = " != ";
                break;

            case GT:
                op = " > ";
                break;
            case LT:
                op = " < ";
                break;
            case LE:
                op = " >= ";
                break;
            case GE:
                op = " <= ";
                break;

            case LIKE:
                op = " LIKE ";
                break;
            case NOT_LIKE:
                op = " NOT LIKE ";
                break;
        }
        sb.write(op).writeValue(value);
    }

    @Override
    public void visitNot(Expression statement) {
        sb.write(" NOT (");
        statement.accept(this);
        sb.write(")");

    }

    @Override
    public void visitMatchAny(QAttribute key, CompareOperator operator, Collection values) {
        if (operator == CompareOperator.EQ || operator == CompareOperator.NE) {
            key.printSQL(sb);
        }
        switch (operator) {
            case NE:
                sb.write("NOT ");
                // no-break;
            case EQ:
                sb.write(" IN(");
                sb.writeValues(values);
                sb.write(")");
                break;

            case NOT_LIKE:
                sb.write("NOT ");
                // no-break;
            case LIKE:
                sb.write("(");
                boolean first = true;
                for (Object v : values) {
                    if (first) {
                        first = false;
                    } else {
                        sb.writeQuoted(" OR ");
                    }
                    key.printSQL(sb);
                    sb.write(" LIKE ");
                    sb.writeQuoted(v);
                }
                break;

            default:
                throw new RuntimeException("Invalid match any operator: " + operator);
        }
    }

    @Override
    public void visitIsNull(QAttribute key, CompareOperator operator) {
        String value;
        switch (operator) {
            case EQ:
                value = " IS NULL";
                break;
            case NE:
                value = " IS NOT NULL";
                break;
            default:
                throw new RuntimeException("Invalid match operator with null value: " + operator);
        }
        key.printSQL(sb);
        sb.write(value);
    }

    @Override
    public void visitAlwaysTrue() {
        sb.write("true");
    }

    @Override
    public void visitPredicateSet(ArrayList<Predicate> predicates, Conjunction conjunction) {
        sb.write("(");
        boolean first = true;
        int cnt_predicate = predicates.size();
        for (int i = 0; i < cnt_predicate; i++) {
            if (first) {
                first = false;
            } else {
                sb.write(conjunction.toString());
            }
            Predicate item = predicates.get(i);
            item.accept(this);
        }
        sb.write(")");
    }

    public void visitJoinedSchema(TableQuery tableQuery) {
        JqlEntityJoin join = tableQuery.getEntityJoin();
        writeJoinStatement(join);
        join = join.getAssociativeJoin();
        if (join != null) {
            writeJoinStatement(join);
        }
//        tableQuery.accept((JqlPredicateVisitor)this);
        tableQuery.accept((JqlEntityJoinVisitor)this);
    }

//    public void visitJoinedSchema(JsonQuery jsonQuery) {
//        jsonQuery.accept((JqlPredicateVisitor)this);
//    }


    private void writeWhere(JqlQuery where) {
        if (!where.isEmpty()) {
            sb.writeRaw("\nWHERE ");
            where.accept((JqlPredicateVisitor) this);
        }
    }

    private void writeJoinStatement(JqlEntityJoin joinKeys) {
        boolean isInverseMapped = joinKeys.isInverseMapped();
        String joinedTable = joinKeys.getJoinedSchema().getTableName();
        sb.write("\nleft outer join ").write(joinedTable).write(" on\n\t");
        for (JqlColumn fk : joinKeys.getForeignKeyColumns()) {
            JqlColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            sb.write(joinKeys.getBaseSchema().getTableName()).write(".").write(anchor.getColumnName());
            sb.write(" = ").write(linked.getSchema().getTableName()).write(".")
                    .write(linked.getColumnName()).write(" and\n\t");
        }
        sb.shrinkLength(6);
    }

    private void writeFrom(JqlQuery where) {
        sb.write("FROM ").write(where.getSchema().getTableName());
        where.accept((JqlEntityJoinVisitor)this);
//        for (JqlEntityJoin join : where.getEntityJoins()) {
//            writeJoinStatement(join);
//            join = join.getAssociativeJoin();
//            if (join != null) {
//                writeJoinStatement(join);
//            }
//        }
    }


    private SqlBuilder writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            sb.writeRaw(schema.getTableName()).write('.');
        }
        sb.writeRaw(name);
        return this;
    }

    private SqlBuilder writeTableName() {
        sb.writeRaw(this.schema.getTableName());
        return this;
    }

    private SqlBuilder writeEquals(String column, Object value) {
        sb.write(column).write(" = ").writeValue(value);
        return this;
    }

    public String createCountQuery(JqlQuery where) {
        sb.write("\nSELECT count(*) ");
        writeFrom(where);
        this.writeWhere(where);
        String sql = sb.reset();
        return sql;
    }

    public String createSelectQuery(JqlQuery where) {
        sb.write("\nSELECT ");
        if (true) {
            for (JqlResultMapping fetch : where.getResultMappings()) {
                JqlSchema table = fetch.getSchema();
                sb.write(table.getTableName()).write(".*, ");
            }
        } else {
            for (JqlResultMapping fetch : where.getResultMappings()) {
                JqlSchema table = fetch.getSchema();
                for (JqlColumn col : table.getReadableColumns()) {
                    sb.write(table.getTableName()).write('.').write(col.getColumnName()).
                            write(" as ").write('\"').write(col.getJsonKey()).write("\",\n");
                }
            }
        }
        sb.replaceTrailingComma("\n");
        writeFrom(where);
        writeWhere(where);
        String sql = sb.reset();
        return sql;
    }

    public String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet) {
        sb.write("\nUPDATE ").write(schema.getTableName()).write(" SET\n");

        for (Map.Entry<String, Object> entry : updateSet.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sb.write("  ");
            this.writeEquals(key, value);
            sb.write(",\n");
        }
        sb.replaceTrailingComma("\n");
        this.writeWhere(where);
        String sql = sb.reset();
        return sql;

    }

    public String createDeleteQuery(JqlQuery where) {
        sb.write("\nDELETE ");
        this.writeFrom(where);
        this.writeWhere(where);
        String sql = sb.reset();
        return sql;
    }

    public String prepareFindByIdStatement() {
        sb.write("\nSELECT * FROM ").write(schema.getTableName()).write("\nWHERE ");
        List<JqlColumn> keys = schema.getPKColumns();
        for (int i = 0; i < keys.size(); ) {
            String key = keys.get(i).getColumnName();
            sb.write(key).write(" = ? ");
            if (++ i < keys.size()) {
                sb.write(" AND ");
            }
        }
        String sql = sb.reset();
        return sql;
    }

    protected String getCommand(Command command) {
        return command.toString();
    }

    public String createInsertStatement(Map entity, boolean ignoreConflict) {

        Set<String> keys = ((Map<String, ?>)entity).keySet();
        sb.writeln();
        sb.write(getCommand(Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
        sb.incTab();
        writeColumnNames(schema.getPhysicalColumnNames(keys), false);
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
        String sql = sb.reset();
        return sql;
    }

    public String prepareBatchInsertStatement(boolean ignoreConflict) {
        sb.writeln();
        sb.write(getCommand(Command.Insert)).write(" INTO ").write(schema.getTableName()).writeln("(");
        for (JqlColumn col : schema.getWritableColumns()) {
            sb.write(col.getColumnName()).write(", ");
        }
        sb.replaceTrailingComma("\n) VALUES (");
        for (JqlColumn col : schema.getWritableColumns()) {
            sb.write("?,");
        }
        sb.replaceTrailingComma(")");
        if (ignoreConflict) {
            sb.write("\nON CONFLICT DO NOTHING");
        }
        String sql = sb.reset();
        return sql;
    }
}
