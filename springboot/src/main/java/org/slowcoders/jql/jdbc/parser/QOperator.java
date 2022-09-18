package org.slowcoders.jql.jdbc.parser;

import java.util.Collection;

public interface QOperator extends QExpression {

    class FilterOp implements QOperator {
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


    class BinaryOp implements QOperator {
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


    class UnaryOp implements QOperator {
        private QExpression statement;
        private String operator;

        UnaryOp(QExpression statement, String operator) {
            this.statement = statement;
            this.operator = operator;
        }

        @Override
        public void printSQL(SQLWriter sb) {
            sb.write(operator).write("(");
            statement.printSQL(sb);
            sb.write(")");
        }

        public static UnaryOp not(QExpression condition) {
            return new UnaryOp(condition, " NOT ");
        }
    }


    class PostOp implements QOperator {
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
