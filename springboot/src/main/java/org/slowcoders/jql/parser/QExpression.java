package org.slowcoders.jql.parser;

public interface QExpression {

    void printSQL(SQLWriter builder);

    default boolean isEmpty() { return false; }

}
