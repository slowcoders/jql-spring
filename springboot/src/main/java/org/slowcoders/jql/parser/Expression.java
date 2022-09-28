package org.slowcoders.jql.parser;

interface Expression {

    void printSQL(SQLWriter builder);

    default boolean isEmpty() { return false; }

}
