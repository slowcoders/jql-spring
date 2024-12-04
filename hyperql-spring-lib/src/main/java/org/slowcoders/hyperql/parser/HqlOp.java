package org.slowcoders.hyperql.parser;

public enum HqlOp {
    EQ, NE,

    LIKE, NOT_LIKE,

    LT, LE,

    GT, GE,

    RE, NOT_RE,

    CONTAINS, OVERLAPS,
    NOT_CONTAINS, NOT_OVERLAPS,

    RE_ignoreCase, NOT_RE_ignoreCase;
}
