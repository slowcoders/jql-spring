package org.slowcoders.jql.parser;

interface Expression {

    void accept(JqlVisitor visitor);

    default boolean isEmpty() { return false; }

}
