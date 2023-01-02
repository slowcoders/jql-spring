package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JQColumn;
import org.eipgrid.jql.JQJoin;
import org.eipgrid.jql.JQSchema;

import java.util.List;

public interface JQResultMapping {
    JQResultMapping getParentNode();

    JQSchema getSchema();

    String getMappingAlias();

    JQJoin getEntityJoin();

    List<JQColumn> getSelectedColumns();

    String[] getEntityMappingPath();

    boolean isArrayNode();

    // It has not any array node in descendants.
    boolean isLinearNode();

    boolean isEmpty();
}
