package org.slowcoders.jql.parser;

import java.util.ArrayList;

class QuerySet implements QExpression {
    Conjunction conjunction;
    ArrayList<QExpression> expressions = new ArrayList<>();

    public enum Conjunction {
        AND(" and "),
        OR(" or ");

        private final String delimiter;

        Conjunction(String delimiter) {
            this.delimiter = delimiter;
        }

        @Override
        public String toString() {
            return delimiter;
        }
    }

    public QuerySet(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public void add(QExpression expression) {
        this.expressions.add(expression);
    }

    @Override
    public void printSQL(SQLWriter sb) {
        if (expressions.size() == 0) {
            sb.write("true");
            return;
        }

        QExpression first = expressions.get(0);
        if (expressions.size() == 1) {
            first.printSQL(sb);
            return;
        }
        sb.write("(");
        first.printSQL(sb);
        for (int i = 0; ++i < expressions.size(); ) {
            QExpression item = expressions.get(i);
            sb.write(conjunction.toString());
            item.printSQL(sb);
        }
        sb.write(")");
    }
}
