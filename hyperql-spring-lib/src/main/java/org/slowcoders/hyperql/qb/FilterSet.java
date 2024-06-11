package org.slowcoders.hyperql.qb;

import org.slowcoders.hyperql.parser.Conjunction;

public class FilterSet implements Filter {

    private final Conjunction conjunction;
    private final Filter[] predicates;

    public FilterSet(Conjunction conjunction, Filter[] predicates) {
        this.conjunction = conjunction;
        this.predicates = predicates;
    }

    public Filter[] entries() {
        return predicates;
    }

    @Override
    public Object getValue() {
        return predicates;
    }

    @Override
    public String getOperator() {
        return conjunction.name();
    }
}
