package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QJoin;
import org.eipgrid.jql.schema.QSchema;

import java.util.List;

public interface JQResultMapping {
    JQResultMapping getParentNode();

    QSchema getSchema();

    String getMappingAlias();

    QJoin getEntityJoin();

    List<QColumn> getSelectedColumns();

    String[] getEntityMappingPath();

    boolean isArrayNode();

    boolean hasArrayDescendantNode();

    boolean isEmpty();
}
