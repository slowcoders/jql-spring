package org.slowcoders.hyperql;

import org.slowcoders.hyperql.schema.QColumn;

import java.util.List;

public interface JqlAccessGuard {
    String getFilterSql(String entityAlias, List<QColumn> columns);
}
