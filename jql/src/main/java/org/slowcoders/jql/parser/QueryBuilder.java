package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface QueryBuilder {
    String createSelectQuery(JqlQuery where);

    String createCountQuery(JqlQuery where);

    String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet);

    String createDeleteQuery(JqlQuery where);

    String prepareFindByIdStatement();

    String createInsertStatement(Map entity, boolean ignoreConflict);
}
