package org.slowcoders.jql.parser;

public interface JqlEntityJoinVisitor {
    void visitJoinedSchema(RowFilter rowFilter);

//    void visitJoinedSchema(JsonFilter jsonFilter);
}
