package org.eipgrid.jql.parser;

public interface Expression {

    void accept(AstVisitor visitor);

    default boolean isEmpty() { return false; }

}
