package org.eipgrid.jql.parser;

import org.eipgrid.jql.JsonNodeType;

import java.util.*;

public class SqlConverter implements JqlPredicateVisitor {
    protected final SourceWriter sw;
    private JqlFilterNode currentNode;

    public enum Command {
        Insert,
        Delete,
        Update,
    }

    public SqlConverter(SourceWriter sw) {
        this.sw = sw;
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
                sw.writeQuoted(node.getMappingAlias());
            } else {
                sw.write(node.getMappingAlias());
            }
            sw.write("->");
        } else {
            sw.write(node.getMappingAlias()).write('.');
        }
    }

    private void writeTypeCast(Class valueType) {
        JsonNodeType vf = JsonNodeType.getNodeType(valueType);
        switch (vf) {
            case Integer:
            case Float:
                sw.write("::NUMERIC");
                break;
            case Date:
                sw.write("::DATE");
                break;
            case Time:
                sw.write("::TIME");
                break;
            case Timestamp:
                sw.write("::TIMESTAMP");
                break;
            case Text:
                sw.write("::TEXT");
                break;
            case Array:
            case Object:
                sw.write("::JSONB");
                break;
        }
    }

    private void writeQualifiedName(String name, Object value) {
        if (!currentNode.isJsonNode()) {
            sw.write(this.currentNode.getMappingAlias()).write('.').write(name);
        }
        else {
            writeJsonPath(currentNode);
            sw.writeQuoted(name);
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
        sw.write(op).writeValue(value);
    }

    @Override
    public void visitNot(Expression statement) {
        sw.write(" NOT ");
        statement.accept(this);
//        out.write(")");
    }

    @Override
    public void visitMatchAny(QAttribute key, CompareOperator operator, Collection values) {
        if (operator == CompareOperator.EQ || operator == CompareOperator.NE) {
            writeQualifiedName(key.getColumnName(), values);
        }
        switch (operator) {
            case NE:
                sw.write("NOT");
                // no-break;
            case EQ:
                sw.write(" IN(");
                sw.writeValues(values);
                sw.write(")");
                break;

            case NOT_LIKE:
                sw.write("NOT ");
                // no-break;
            case LIKE:
                sw.write("(");
                boolean first = true;
                for (Object v : values) {
                    if (first) {
                        first = false;
                    } else {
                        sw.write(" OR ");
                    }
                    writeQualifiedName(key.getColumnName(), "");
                    sw.write(" LIKE ");
                    sw.writeQuoted(v);
                }
                sw.write(")");
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
        sw.write(value);
    }

    @Override
    public void visitAlwaysTrue() {
        sw.write("true");
    }

    @Override
    public void visitPredicateSet(ArrayList<Predicate> predicates, Conjunction conjunction) {
        sw.write("(");
        boolean first = true;
        int cnt_predicate = predicates.size();
        for (int i = 0; i < cnt_predicate; i++) {
            if (first) {
                first = false;
            } else {
                sw.write(conjunction.toString());
            }
            Predicate item = predicates.get(i);
            item.accept(this);
        }
        sw.write(")");
    }

}
