package org.slowcoders.hyperql.parser;

public interface Expression {

    void accept(PredicateVisitor visitor);

    default boolean isEmpty() { return false; }

}
