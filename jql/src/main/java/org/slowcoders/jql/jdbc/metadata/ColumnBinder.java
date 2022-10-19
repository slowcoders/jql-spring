package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.SchemaLoader;

class ColumnBinder {
    private final SchemaLoader schemaLoader;
    private final String tableName;
    private final String columnName;
    private JqlColumn pk;

    ColumnBinder(SchemaLoader schemaLoader, String tableName, String columnName) {
        this.schemaLoader = schemaLoader;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public JqlColumn getJoinedColumn() {
        if (pk == null) {
            pk = schemaLoader.loadSchema(tableName).getColumn(columnName);
            assert (pk != null);
        }
        return pk;
    }
}
