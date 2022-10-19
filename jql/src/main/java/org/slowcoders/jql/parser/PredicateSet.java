package org.slowcoders.jql.parser;

import java.util.ArrayList;

class PredicateSet implements Predicate {
    Conjunction conjunction;
    ArrayList<Predicate> predicates = new ArrayList<>();

    public PredicateSet(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public void add(Predicate predicate) {
        this.predicates.add(predicate);
    }

    @Override
    public void accept(JqlVisitor sb) {
        if (predicates.size() == 0) {
            sb.visitAlwaysTrue();
            return;
        }

        Predicate first = predicates.get(0);
        if (predicates.size() == 1) {
            first.accept(sb);
            return;
        }
        sb.visitPredicateSet(predicates, conjunction);
    }
}
