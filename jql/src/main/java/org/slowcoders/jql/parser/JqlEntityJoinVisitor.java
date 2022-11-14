package org.slowcoders.jql.parser;

public interface JqlEntityJoinVisitor {
    void visitJoinedSchema(TableFilter tableFilter);

//    void visitJoinedSchema(JsonFilter jsonFilter);
}
