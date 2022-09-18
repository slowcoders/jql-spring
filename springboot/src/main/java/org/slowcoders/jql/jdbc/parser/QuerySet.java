package org.slowcoders.jql.jdbc.parser;

import java.util.ArrayList;

class QuerySet implements QExpression {
    Type delimiter;
    ArrayList<QExpression> expressions = new ArrayList<>();

    public enum Type {
        AND(" and "),
        OR(" or ");

        private final String delimiter;

        Type(String delimiter) {
            this.delimiter = delimiter;
        }

        @Override
        public String toString() {
            return delimiter;
        }
    }

    public QuerySet(Type delimiter) {
        this.delimiter = delimiter;
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
            sb.write(delimiter.toString());
            item.printSQL(sb);
        }
        sb.write(")");
    }
}
