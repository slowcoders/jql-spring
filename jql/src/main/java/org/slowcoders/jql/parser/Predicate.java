package org.slowcoders.jql.parser;

import java.util.Collection;

interface Predicate extends Expression {

    class MatchAny implements Predicate {
        private final QAttribute key;
        private final Collection values;
        private final CompareOperator operator;

        MatchAny(QAttribute key, CompareOperator operator, Collection values) {
            this.key = key;
            this.operator = operator;
            this.values = values;
        }

        @Override
        public void accept(JqlVisitor sb) {
            sb.visitMatchAny(key, operator, values);
        }
    }


    class Compare implements Predicate {
        private final QAttribute key;
        private final Object value;
        private final CompareOperator operator;

        public Compare(QAttribute key, Object value, CompareOperator operator) {
            this.key = key;
            this.value = value;
            this.operator = operator;
        }

        @Override
        public void accept(JqlVisitor sb) {
            if (value == null) {
                sb.visitIsNull(key, operator);
            } else {
                sb.visitCompare(key, operator, value);
            }
        }
    }


    class Not implements Predicate {
        private Expression statement;

        Not(Expression statement) {
            this.statement = statement;
        }

        @Override
        public void accept(JqlVisitor sb) {
            sb.visitNot(statement);
        }
    }

}
