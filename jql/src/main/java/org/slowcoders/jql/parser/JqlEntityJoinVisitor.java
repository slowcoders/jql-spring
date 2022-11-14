package org.slowcoders.jql.parser;

public interface JqlEntityJoinVisitor {
    void visitJoinedSchema(TableQuery tableQuery);

//    void visitJoinedSchema(JsonQuery jsonQuery);
}
