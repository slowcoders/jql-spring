package org.slowcoders.jql.jdbc.storage;

import org.slowcoders.jql.jdbc.JdbcStorage;
import org.slowcoders.jql.schema.QColumn;

class ColumnBinder {
    private final JdbcStorage schemaLoader;
    private final String tableName;
    private final String columnName;
    private QColumn pk;

    ColumnBinder(JdbcStorage schemaLoader, String tableName, String columnName) {
        this.schemaLoader = schemaLoader;
        this.tableName = tableName;
        this.columnName = columnName.toLowerCase();
    }

    public QColumn getJoinedColumn() {
        if (pk == null) {
            pk = schemaLoader.loadSchema(tableName).getColumn(columnName);
            assert (pk != null);
        }
        return pk;
    }
}
