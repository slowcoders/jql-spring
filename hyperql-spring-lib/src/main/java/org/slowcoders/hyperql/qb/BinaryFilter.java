package org.slowcoders.hyperql.qb;

import org.slowcoders.hyperql.parser.HqlOp;

public class BinaryFilter implements Filter {

    private final HqlOp op;
    private final String column;
    private final Object value;

    public BinaryFilter(HqlOp op, String column, Object value) {
        this.op = op;
        this.column = column;
        this.value = value; 
    }

    public static BinaryFilter of(String column, HqlOp op, Object value) {
        return new BinaryFilter(op, column, value);
    }

    @Override
    public String getKey() {
        return column;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getOperator() {
        return op.name();
    }
}
