package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.JqlResultMapping;

import java.util.*;

public class SqlWriter extends SourceWriter<SqlWriter> implements JqlPredicateVisitor {//}, QueryBuilder {
    private final JqlSchema rootSchema;
    private JqlSchema workingSchema;
    private String mappingAlias;

    public enum Command {
        Insert,
        Delete,
        Update,
    }

    public SqlWriter(JqlSchema rootSchema) {
        super('\'');
        this.workingSchema = this.rootSchema = rootSchema;
    }

    public JqlSchema setWorkingSchema(JqlSchema jqlSchema, String mappingAlias) {
        JqlSchema old = this.workingSchema;
        this.workingSchema = jqlSchema;
        this.mappingAlias = mappingAlias;
        return old;
    }

    public void writeColumnNames(Iterable<String> names, boolean withTableName) {
        for (String name : names) {
            writeColumnName(name, withTableName);
            this.write(", ");
        }
        this.shrinkLength(2);
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

            case GE:
                op = " >= ";
                break;
            case GT:
                op = " > ";
                break;

            case LE:
                op = " <= ";
                break;
            case LT:
                op = " < ";
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
                this.write("NOT ");
                // no-break;
            case EQ:
                this.write(" IN(");
                this.writeValues(values);
                this.write(")");
                break;

            case NOT_LIKE:
                this.write("NOT ");
                // no-break;
            case LIKE:
                this.write("(");
                boolean first = true;
                for (Object v : values) {
                    if (first) {
                        first = false;
                    } else {
                        this.writeQuoted(" OR ");
                    }
                    key.printSQL(this);
                    this.write(" LIKE ");
                    this.writeQuoted(v);
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
        this.write(value);
    }

    @Override
    public void visitAlwaysTrue() {
        this.write("true");
    }

    @Override
    public void visitPredicateSet(ArrayList<Predicate> predicates, Conjunction conjunction) {
        this.write("(");
        boolean first = true;
        int cnt_predicate = predicates.size();
        for (int i = 0; i < cnt_predicate; i++) {
            if (first) {
                first = false;
            } else {
                this.write(conjunction.toString());
            }
            Predicate item = predicates.get(i);
            item.accept(this);
        }
        this.write(")");
    }

//    public void visitJoinedSchema(JsonFilter jsonFilter) {
//        jsonFilter.accept((JqlPredicateVisitor)this);
//    }


    private SqlWriter writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            this.writeRaw(mappingAlias).write('.');
        }
        this.writeRaw(name);
        return this;
    }

    public SqlWriter writeEquals(String columnName, Object value) {
        this.write(columnName).write(" = ").writeValue(value);
        return this;
    }


}
