package org.eipgrid.jql.parser;

import java.util.Collection;

interface Predicate extends Expression {

    class MatchAny implements Predicate {
        private final String column;
        private final Collection values;
        private final JqlOp operator;

        MatchAny(String column, JqlOp operator, Collection values) {
            this.column = column;
            this.operator = operator;
            this.values = values;
        }

        @Override
        public void accept(PredicateVisitor sb) {
            sb.visitMatchAny(column, operator, values);
        }
    }


    class Compare implements Predicate {
        private final String column;
        private final Object value;
        private final JqlOp operator;

        public Compare(String column, JqlOp operator, Object value) {
            this.column = column;
            this.value = value;
            this.operator = operator;
        }

        @Override
        public void accept(PredicateVisitor sb) {
            if (value == null) {
                sb.visitCompareNull(column, operator);
            } else {
                sb.visitPredicate(column, operator, value);
            }
        }
    }


    class Not implements Predicate {
        private Expression statement;

        Not(Expression statement) {
            this.statement = statement;
        }

        @Override
        public void accept(PredicateVisitor sb) {
            sb.visitNot(statement);
        }
    }

}
