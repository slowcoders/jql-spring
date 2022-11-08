package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.Collection;

public class SqlBuilder extends SourceWriter<SqlBuilder> implements JqlVisitor, QueryBuilder {
    private JqlSchema schema;

    public SqlBuilder(JqlSchema schema) {
        super('\'');
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
            writeColumnName(name, withTableName).write(", ");
        }
        shrinkLength(2);
    }


    @Override
    public void visitCompare(QAttribute column, CompareOperator operator, Object value) {
        column.printSQL(this);
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
        this.write(op).writeValue(value);
    }

    @Override
    public void visitNot(Expression statement) {
        this.write(" NOT (");
        statement.accept(this);
        this.write(")");

    }

    @Override
    public void visitMatchAny(QAttribute key, CompareOperator operator, Collection values) {
        if (operator == CompareOperator.EQ || operator == CompareOperator.NE) {
            key.printSQL(this);
        }
        switch (operator) {
            case NE:
                write("NOT ");
                // no-break;
            case EQ:
                write(" IN(");
                writeValues(values);
                write(")");
                break;

            case NOT_LIKE:
                write("NOT ");
                // no-break;
            case LIKE:
                write("(");
                boolean first = true;
                for (Object v : values) {
                    if (first) {
                        first = false;
                    } else {
                        writeQuoted(" OR ");
                    }
                    key.printSQL(this);
                    write(" LIKE ");
                    writeQuoted(v);
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
        key.printSQL(this);
        write(value);
    }

    @Override
    public void visitAlwaysTrue() {
        write("true");
    }

    @Override
    public void visitPredicateSet(ArrayList<Predicate> predicates, Conjunction conjunction) {
        write("(");
        boolean first = true;
        int cnt_predicate = predicates.size();
        for (int i = 0; i < cnt_predicate; i++) {
            if (first) {
                first = false;
            } else {
                write(conjunction.toString());
            }
            Predicate item = predicates.get(i);
            item.accept(this);
        }
        write(")");

    }

    public void writeWhere(JqlQuery where, boolean includeTableName) {
        if (includeTableName) {
            writeJoinStatement(where);
        }
        if (!where.isEmpty()) {
            writeRaw("\nWHERE ");
            where.accept(this);
        }
    }

    private void writeJoinStatement(JqlEntityJoin joinKeys) {
        boolean isInverseMapped = joinKeys.isInverseMapped();
        String joinedTable = joinKeys.getJoinedSchema().getTableName();
        write("\nleft outer join ").write(joinedTable).write(" on\n");
        for (JqlColumn fk : joinKeys.getForeignKeyColumns()) {
            JqlColumn anchor, linked;
            if (isInverseMapped) {
                linked = fk; anchor = fk.getJoinedPrimaryColumn();
            } else {
                anchor = fk; linked = fk.getJoinedPrimaryColumn();
            }
            write("  ").write(joinKeys.getBaseSchema().getTableName()).write(".").write(anchor.getColumnName());
            write(" = ").write(linked.getSchema().getTableName()).write(".")
                    .write(linked.getColumnName()).write(" and\n");
        }
        shrinkLength(5);
    }

    public void writeJoinStatement(JqlQuery where) {
        write(where.getSchema().getTableName());
        for (JqlEntityJoin join : where.getForeignKeyBasedJoins()) {
            writeJoinStatement(join);
        }
        for (JqlEntityJoin join : where.getPrimaryKeyBasedJoins()) {
            writeJoinStatement(join);
            join = join.getAssociateJoin();
            if (join != null) {
                writeJoinStatement(join);
            }
        }
    }


    public SqlBuilder writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            writeRaw(schema.getTableName()).write('.');
        }
        writeRaw(name);
        return this;
    }

    public SqlBuilder writeTableName() {
        writeRaw(this.schema.getTableName());
        return this;
    }

    public SqlBuilder writeEquals(String column, Object value) {
        this.write(column).write(" = ").writeValue(value);
        return this;
    }

    public String createSelectQuery(JqlQuery where) {
        write("\nSELECT ");
        if (true) {
            for (JqlResultMapping fetch : where.getResultMappings()) {
                JqlSchema table = fetch.getSchema();
                write(table.getTableName()).write(".*, ");
            }
        } else {
            for (JqlResultMapping fetch : where.getResultMappings()) {
                JqlSchema table = fetch.getSchema();
                for (JqlColumn col : table.getReadableColumns()) {
                    write(table.getTableName()).write('.').write(col.getColumnName()).
                            write(" as ").write('\"').write(col.getJsonKey()).write("\",\n");
                }
            }
        }
        replaceTrailingComma("\nFROM ");
        writeWhere(where, true);
        String sql = toString();
        super.clear();
        return sql;
    }
}
