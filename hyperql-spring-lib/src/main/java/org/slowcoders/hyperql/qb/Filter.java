package org.slowcoders.hyperql.qb;

import org.slowcoders.hyperql.parser.*;

public interface Filter {
    static final Filter[] EMPTY_FILTERS = new Filter[0];

    default Filter[] entries() {
        return EMPTY_FILTERS;
    }

    default String getKey() {
        return null;
    }

    Object getValue();

    String getOperator();

    static Filter _in_(String subEntity, Filter ...filters) {
        return new JoinedFilter(subEntity, filters);
    }

    static Filter _and_(Filter...filters) {
        return new FilterSet(Conjunction.AND, filters);
    }

    static Filter _or_(Filter...filters) {
        return new FilterSet(Conjunction.OR, filters);
    }

    static Filter _not_(Filter exp) {
        return new NotFilter(exp);
    }

    static Filter _eq_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.EQ, value);
    }

    static Filter _ne_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.EQ, value);
    }

    static Filter _le_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.LE, value);
    }

    static Filter _ge_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.GE, value);
    }

    static Filter _lt_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.LT, value);
    }

    static Filter _gt_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.GT, value);
    }

    static Filter _like_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.LIKE, value);
    }

    static Filter _not_like_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.NOT_LIKE, value);
    }

    static Filter _re_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.RE, value);
    }

    static Filter _not_re_(String column, Object value) {
        return BinaryFilter.of(column, HqlOp.NOT_RE, value);
    }

}
