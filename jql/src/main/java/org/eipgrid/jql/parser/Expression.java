package org.eipgrid.jql.parser;

interface Expression {

    void accept(JqlPredicateVisitor visitor);

    default boolean isEmpty() { return false; }

}
