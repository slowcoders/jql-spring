package org.slowcoders.hyperql.qb;

public class NotFilter implements Filter {

    private final Filter expression;

    public NotFilter(Filter expression) {
        this.expression = expression;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public Object getValue() {
        return expression;
    }

    @Override
    public String getOperator() {
        return "not";
    }

}
