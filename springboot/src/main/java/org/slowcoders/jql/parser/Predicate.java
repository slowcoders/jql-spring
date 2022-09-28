package org.slowcoders.jql.parser;

import java.util.Collection;

interface Predicate extends Expression {

    class FilterOp implements Predicate {
        private final QAttribute key;
        private final Collection values;
        private final String operator;

        FilterOp(QAttribute key, String operator, Collection values) {
            this.key = key;
            this.operator = operator;
            this.values = values;
        }

        @Override
        public void printSQL(SQLWriter sb) {
            key.printSQL(sb);
            sb.write(operator).write("(");
            sb.writeValues(values);
            sb.write(")");
        }
    }


    class BinaryOp implements Predicate {
        private final QAttribute key;
        private final Object value;
        private final String operator;

        public BinaryOp(QAttribute key, Object value, String operator) {
            this.key = key;
            this.value = value;
            this.operator = operator;
        }

        @Override
        public void printSQL(SQLWriter sb) {
            sb.writePredicate(key, operator, value);
        }
    }


    class UnaryOp implements Predicate {
        private Expression statement;
        private String operator;

        UnaryOp(Expression statement, String operator) {
            this.statement = statement;
            this.operator = operator;
        }

        @Override
        public void printSQL(SQLWriter sb) {
            sb.write(operator).write("(");
            statement.printSQL(sb);
            sb.write(")");
        }

        public static UnaryOp not(Expression condition) {
            return new UnaryOp(condition, " NOT ");
        }
    }


    class PostOp implements Predicate {
        private QAttribute key;
        private String operator;

        PostOp(QAttribute key, String operator) {
            this.key = key;
            this.operator = operator;
        }

        @Override
        public void printSQL(SQLWriter sb) {
            key.printSQL(sb);
            sb.write(operator);
        }
    }
}
