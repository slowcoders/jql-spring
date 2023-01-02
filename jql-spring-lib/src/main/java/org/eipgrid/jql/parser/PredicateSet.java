package org.eipgrid.jql.parser;

import java.util.ArrayList;

class PredicateSet extends ArrayList<Expression> implements Predicate {
    private final Conjunction conjunction;
    private final JqlFilter baseFilter;

    public PredicateSet(Conjunction conjunction) {
        this(conjunction, null);
    }

    protected PredicateSet(Conjunction conjunction, JqlFilter baseFilter) {
        this.conjunction = conjunction;
        this.baseFilter = baseFilter;
    }

    JqlFilter getBaseFilter() { return baseFilter; }

    public boolean add(Expression predicate) {
        return super.add(predicate);
    }

    @Override
    public void accept(AstVisitor sb) {
        if (size() == 0) {
            sb.visitAlwaysTrue();
            return;
        }
        Expression first = super.get(0);
        if (super.size() == 1) {
            first.accept(sb);
        } else {
            sb.visitPredicates(this, conjunction);
        }
    }
}
