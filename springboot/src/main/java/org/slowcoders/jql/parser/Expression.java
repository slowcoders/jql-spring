package org.slowcoders.jql.parser;

interface Expression {

    void buildQuery(QueryBuilder writer);

    default boolean isEmpty() { return false; }

}
