package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JQType;
import org.eipgrid.jql.parser.*;
import org.eipgrid.jql.util.SourceWriter;

import java.util.*;

public class SqlConverter implements PredicateVisitor {
    protected final SourceWriter sw;
    private EntityFilter currentNode;

    public enum Command {
        Insert,
        Delete,
        Update,
    }

    public SqlConverter(SourceWriter sw) {
        this.sw = sw;
    }

    public void visitNode(EntityFilter node) {
        EntityFilter old = this.currentNode;
        this.currentNode = node;
//        node.getPredicates().accept(this);
//        this.currentNode = old;
    }

    private void writeJsonPath(EntityFilter node) {
        if (node.isJsonNode()) {
            EntityFilter parent = node.getParentNode();
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
        JQType vf = JQType.of(valueType);
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
    public void visitPredicate(String column, JqlOp operator, Object value) {
        writeQualifiedName(column, value);
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
    }

    @Override
    public void visitMatchAny(String column, JqlOp operator, Collection values) {
        if (operator == JqlOp.EQ || operator == JqlOp.NE) {
            writeQualifiedName(column, values);
        }
        switch (operator) {
            case NE:
                sw.write(" NOT");
                // no-break;
            case EQ:
                sw.write(" IN(");
                sw.writeValues(values);
                sw.write(")");
                break;

            case NOT_LIKE:
                sw.write(" NOT ");
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
                    writeQualifiedName(column, "");
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
    public void visitCompareNull(String column, JqlOp operator) {
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
        writeQualifiedName(column, null);
        //key.printSQL(this);
        sw.write(value);
    }

    @Override
    public void visitAlwaysTrue() {
        sw.write("true");
    }

    @Override
    public void visitPredicates(Collection<Expression> predicates, Conjunction conjunction) {
        sw.write("(");
        boolean first = true;
        for (Expression item : predicates) {
            if (item.isEmpty()) continue;

            if (first) {
                first = false;
            } else {
                sw.write(conjunction.toString());
            }
            item.accept(this);
        }
        sw.write(")");
    }

}
