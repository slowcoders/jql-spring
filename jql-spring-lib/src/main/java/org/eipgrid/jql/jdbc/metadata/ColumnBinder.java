package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.SchemaLoader;

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

    public JqlColumn getJoinedColumn(Class<?> javaType) {
        if (pk == null) {
            pk = schemaLoader.loadSchema(tableName, javaType).getColumn(columnName);
            assert (pk != null);
        }
        return pk;
    }
}
