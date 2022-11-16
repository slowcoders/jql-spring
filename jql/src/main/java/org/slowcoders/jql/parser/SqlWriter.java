package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.JqlResultMapping;

import java.util.*;

public class SqlWriter extends SourceWriter<SqlWriter> implements JqlPredicateVisitor {//}, QueryBuilder {
    private final JqlSchema rootSchema;
    private JqlSchema workingSchema;
    private final SourceWriter<SourceWriter> sb = new SourceWriter<SourceWriter>('\'');
    protected ArrayList<JqlResultMapping> rowMappings = new ArrayList<>();

    public enum Command {
        Insert,
        Delete,
        Update,
    }

    public SqlWriter(JqlSchema rootSchema) {
        super('\'');
        this.workingSchema = this.rootSchema = rootSchema;
    }

    public JqlSchema getRootSchema() {
        return this.rootSchema;
    }

    public JqlSchema getWorkingSchema() {
        return workingSchema;
    }

    public ArrayList<JqlResultMapping> getRowMappings() {
        return rowMappings;
    }

    public JqlSchema setWorkingSchema(JqlSchema jqlSchema) {
        JqlSchema old = this.workingSchema;
        this.workingSchema = jqlSchema;
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

//    public void visitJoinedSchema(JsonFilter jsonFilter) {
//        jsonFilter.accept((JqlPredicateVisitor)this);
//    }


    private SqlWriter writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            sb.writeRaw(workingSchema.getTableName()).write('.');
        }
        sb.writeRaw(name);
        return this;
    }

    private SqlWriter writeTableName() {
        sb.writeRaw(this.workingSchema.getTableName());
        return this;
    }

    public SqlWriter writeEquals(String columnName, Object value) {
        sb.write(columnName).write(" = ").writeValue(value);
        return this;
    }


}
