package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.Collection;

public class QueryBuilder extends SourceWriter<QueryBuilder> implements JqlVisitor {
    private JqlSchema schema;

    public QueryBuilder(JqlSchema schema) {
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

    @Override
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
        switch (operator) {
            case EQ:
                if (value == null) {
                    value = " IS NULL";
                } else {
                    op = " = ";
                }
                break;
            case NE:
                if (value == null) {
                    value = " IS NOT NULL";
                } else {
                    op = " != ";
                }
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
    public void visitIsNull(QAttribute key, boolean isNull) {
        key.printSQL(this);
        write(" IS NULL");
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

    @Override
    public void writeWhere(JqlQuery where, boolean includeTableName) {
        if (includeTableName) {
            writeJoinStatement(where);
        }
        if (!where.isEmpty()) {
            writeRaw("\nWHERE ");
            where.accept(this);
        }
    }


    public void writeJoinStatement(JqlQuery where) {
        write(where.getSchema().getTableName());
        for (JqlEntityJoin joinKeys : where.getJoinList()) {
            boolean isInverseMapped = joinKeys.isInverseMapped();
            String joinedTable = joinKeys.getJoinedSchema().getTableName();
            write("\nleft outer join ").write(joinedTable).write(" on\n");
            for (JqlColumn fk : joinKeys) {
                JqlColumn anchor, linked;
                if (isInverseMapped) {
                    linked = fk; anchor = fk.getJoinedPrimaryColumn();
                } else {
                    anchor = fk; linked = fk.getJoinedPrimaryColumn();
                }
                write("  ").write(joinKeys.getAnchorSchema().getTableName()).write(".").write(anchor.getColumnName());
                write(" = ").write(linked.getSchema().getTableName()).write(".")
                        .write(linked.getColumnName()).write(" and\n");
            }
            shrinkLength(5);
        }
    }


    @Override
    public QueryBuilder writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            writeRaw(schema.getTableName()).write('.');
        }
        writeRaw(name);
        return this;
    }

    @Override
    public QueryBuilder writeColumnName(String name) {
        return writeColumnName(name, true);
    }

    @Override
    public QueryBuilder writeTableName() {
        writeRaw(this.schema.getTableName());
        return this;
    }

    public QueryBuilder writeEquals(String column, Object value) {
        this.write(column).write(" = ").writeValue(value);
        return this;
    }

}
