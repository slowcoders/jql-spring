package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.JQColumn;
import org.eipgrid.jql.JQSchemaLoader;

class ColumnBinder {
    private final JQSchemaLoader schemaLoader;
    private final String tableName;
    private final String columnName;
    private JQColumn pk;

    ColumnBinder(JQSchemaLoader schemaLoader, String tableName, String columnName) {
        this.schemaLoader = schemaLoader;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public JQColumn getJoinedColumn(Class<?> javaType) {
        if (pk == null) {
            pk = schemaLoader.loadSchema(tableName, javaType).getColumn(columnName);
            assert (pk != null);
        }
        return pk;
    }
}
