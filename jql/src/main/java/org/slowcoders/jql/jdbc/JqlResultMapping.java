package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.List;

public interface JqlResultMapping {
    JqlSchema getSchema();

    List<JqlColumn> getSelectedColumns();

    String[] getEntityMappingPath();

    String getMappingAlias();

    JqlResultMapping getParentNode();

    JqlSchemaJoin getEntityJoin();

    boolean isArrayNode();

}
