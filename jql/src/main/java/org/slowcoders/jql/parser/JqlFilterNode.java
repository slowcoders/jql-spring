package org.slowcoders.jql.parser;

public interface JqlFilterNode {

    String getMappingAlias();

    JqlFilterNode getParentNode();

    boolean isJsonNode();
}
