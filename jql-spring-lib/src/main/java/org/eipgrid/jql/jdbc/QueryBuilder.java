package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.parser.AstRoot;

import java.util.Map;

public interface QueryBuilder {
    String createSelectQuery(AstRoot where, JqlSelect columns);

    String createCountQuery(AstRoot where);

    String createUpdateQuery(AstRoot where, Map<String, Object> updateSet);

    String createDeleteQuery(AstRoot where);

    String prepareFindByIdStatement(JqlSchema schema);

    String createInsertStatement(JqlSchema schema, Map entity, boolean ignoreConflict);
}
