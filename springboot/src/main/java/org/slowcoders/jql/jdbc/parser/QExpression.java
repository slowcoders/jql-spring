package org.slowcoders.jql.jdbc.parser;

public interface QExpression {

    void printSQL(SQLWriter builder);

    default boolean isEmpty() { return false; }

}
