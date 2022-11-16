package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.List;

public interface JqlResultMapping {
    JqlSchema getSchema();

    List<JqlColumn> getSelectColumns();

    String[] getEntityMappingPath();

    JqlEntityJoin getEntityJoin();

    default boolean hasSelectedColumns() {
        return getSelectColumns() == null || getSelectColumns().size() > 0;
    }
}
