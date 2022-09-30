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
    public void buildQuery(QueryBuilder sb) {
        if (expressions.size() == 0) {
            sb.write("true");
            return;
        }

        Predicate first = expressions.get(0);
        if (expressions.size() == 1) {
            first.buildQuery(sb);
            return;
        }
        sb.write("(");
        first.buildQuery(sb);
        for (int i = 0; ++i < expressions.size(); ) {
            Predicate item = expressions.get(i);
            sb.write(conjunction.toString());
            item.buildQuery(sb);
        }
        sb.write(")");
    }
}
