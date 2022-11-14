package org.slowcoders.jql.parser;

import java.util.ArrayList;

class PredicateSet extends ArrayList<Predicate> implements Predicate {
    private final Conjunction conjunction;

    public PredicateSet(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    EntityQuery getEntityPredicates() { return null; }

    public boolean add(Predicate predicate) {
        return super.add(predicate);
    }

    @Override
    public void accept(JqlPredicateVisitor sb) {
        if (super.size() == 0) {
            sb.visitAlwaysTrue();
            return;
        }

        Predicate first = super.get(0);
        if (super.size() == 1) {
            first.accept(sb);
        } else {
            sb.visitPredicateSet(this, conjunction);
        }
    }
}
