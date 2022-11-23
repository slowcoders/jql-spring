package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.JqlSchema;

import java.util.List;

public interface JqlResultMapping {
    JqlSchema getSchema();

    List<JqlColumn> getSelectedColumns();

    String[] getEntityMappingPath();

    String getMappingAlias();

    JqlResultMapping getParentNode();

    JqlSchemaJoin getSchemaJoin();

    boolean isArrayNode();

    boolean hasFilterPredicates();
}
