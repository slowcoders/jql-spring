package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.schema.SchemaLoader;

class ColumnBinder {
    private final SchemaLoader schemaLoader;
    private final String tableName;
    private final String columnName;
    private final String fkConstraint;
    private QColumn pk;

    ColumnBinder(SchemaLoader schemaLoader, String tableName, String columnName, String fkConstraint) {
        this.schemaLoader = schemaLoader;
        this.tableName = tableName;
        this.columnName = columnName.toLowerCase();
        this.fkConstraint = fkConstraint;
    }

    public QColumn getJoinedColumn() {
        if (pk == null) {
            QSchema pkSchema = schemaLoader.loadSchema(tableName);
            pk = pkSchema.getColumn(columnName);
            assert (pk != null);
        }
        return pk;
    }
}
