package org.slowcoders.hyperql.schema;

import java.util.List;
import java.util.Map;

public interface EntityAccessGuard {
    default String checkReadable(QSchema schema, String entityAlias, List<QColumn> columns) throws SecurityException {
        return null;
    }

    default String checkInsertable(QSchema schema, String entityAlias, Map<String, Object> entity) throws SecurityException {
        return null;
    }

    default String checkUpdatable(QSchema schema, String entityAlias, Map<String, Object> entity) throws SecurityException {
        return null;
    }

    default String checkDeletable(QSchema schema, String entityAlias) throws SecurityException {
        return null;
    }
}
