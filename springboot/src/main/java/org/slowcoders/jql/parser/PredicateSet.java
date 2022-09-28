package org.slowcoders.jql.parser;

import java.util.ArrayList;

class PredicateSet implements Predicate {
    Conjunction conjunction;
    ArrayList<Predicate> expressions = new ArrayList<>();

    public PredicateSet(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public void add(Predicate predicate) {
        this.expressions.add(predicate);
    }

    @Override
    public void printSQL(SQLWriter sb) {
        if (expressions.size() == 0) {
            sb.write("true");
            return;
        }

        Predicate first = expressions.get(0);
        if (expressions.size() == 1) {
            first.printSQL(sb);
            return;
        }
        sb.write("(");
        first.printSQL(sb);
        for (int i = 0; ++i < expressions.size(); ) {
            Predicate item = expressions.get(i);
            sb.write(conjunction.toString());
            item.printSQL(sb);
        }
        sb.write(")");
    }
}
