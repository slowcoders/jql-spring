package org.eipgrid.jql.parser;

import org.eipgrid.jql.JsonNodeType;

import java.util.*;

public class SqlWriter extends SourceWriter<SqlWriter> implements JqlPredicateVisitor {//}, QueryBuilder {
    private JqlFilterNode currentNode;

    public enum Command {
        Insert,
        Delete,
        Update,
    }

    public SqlWriter() {
        super('\'');
    }

    public JqlFilterNode setCurrentNode(JqlFilterNode node) {
        JqlFilterNode old = this.currentNode;
        this.currentNode = node;
        return old;
    }

    private void writeJsonPath(JqlFilterNode node) {
        if (node.isJsonNode()) {
            JqlFilterNode parent = node.getParentNode();
            writeJsonPath(parent);
            if (parent.isJsonNode()) {
                writeQuoted(node.getMappingAlias());
            } else {
                write(node.getMappingAlias());
            }
            write("->");
        } else {
            write(node.getMappingAlias()).write('.');
        }
    }

    private void writeTypeCast(Class valueType) {
        JsonNodeType vf = JsonNodeType.getNodeType(valueType);
        switch (vf) {
            case Integer:
            case Float:
                write("::NUMERIC");
                break;
            case Date:
                write("::DATE");
                break;
            case Time:
                write("::TIME");
                break;
            case Timestamp:
                write("::TIMESTAMP");
                break;
            case Text:
                write("::TEXT");
                break;
            case Array:
            case Object:
                write("::JSONB");
                break;
        }
    }

    private void writeQualifiedName(String name, Object value) {
        if (!currentNode.isJsonNode()) {
            write(this.currentNode.getMappingAlias()).write('.').write(name);
        }
        else {
            writeJsonPath(currentNode);
            writeQuoted(name);
            if (value != null) {
                writeTypeCast(value.getClass());
            }
        }
    }

    @Override
    public void visitCompare(QAttribute column, CompareOperator operator, Object value) {
        writeQualifiedName(column.getColumnName(), value);
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
        this.write(" NOT ");
        statement.accept(this);
//        this.write(")");
    }

    @Override
    public void visitMatchAny(QAttribute key, CompareOperator operator, Collection values) {
        if (operator == CompareOperator.EQ || operator == CompareOperator.NE) {
            writeQualifiedName(key.getColumnName(), values);
        }
        switch (operator) {
            case NE:
                this.write("NOT");
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
                        this.write(" OR ");
                    }
                    writeQualifiedName(key.getColumnName(), "");
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
        writeQualifiedName(key.getColumnName(), null);
        //key.printSQL(this);
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

}
