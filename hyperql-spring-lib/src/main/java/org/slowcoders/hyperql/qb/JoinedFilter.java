package org.slowcoders.hyperql.qb;

import org.slowcoders.hyperql.parser.HqlOp;

public class JoinedFilter implements Filter {

    private final String column;
    private final Filter[] filters;

    public JoinedFilter(String column, Filter[] filters) {
        this.column = column;
        this.filters = filters;
    }

    public Filter[] entries() {
        return filters;
    }


    @Override
    public String getKey() {
        return column;
    }

    @Override
    public Object getValue() {
        return filters;
    }

    @Override
    public String getOperator() {
        return null;
    }

}
