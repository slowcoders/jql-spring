package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.List;

public interface JqlResultMapping {
    String[] getEntityMappingPath();

    JqlSchema getSchema();

    List<JqlColumn> getSelectColumns();

    JqlEntityJoin getEntityJoin();

    boolean hasSelectedColumns();
}
