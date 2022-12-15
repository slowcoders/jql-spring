package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.JqlSchema;

import java.util.List;
import java.util.Set;

public interface JqlResultMapping {
    JqlResultMapping getParentNode();

    JqlSchema getSchema();

    String getMappingAlias();

    JqlSchemaJoin getSchemaJoin();

    List<JqlColumn> getSelectedColumns();

    String[] getEntityMappingPath();

    boolean isArrayNode();

    // It has not any array node in descendants.
    boolean isLinearNode();

    boolean isEmpty();
}
