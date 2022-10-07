package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;

class ColumnMappingHelper {
    private final JqlSchema pkSchema;
    private final JqlSchema fkSchema;
    ArrayList<JqlColumn> fkColumns = new ArrayList<>();
    public ColumnMappingHelper(JqlSchema pkSchema, JqlSchema fkSchema) {
        this.pkSchema = pkSchema;
        this.fkSchema = fkSchema;
    }

    public void addMappedForeignKey(JqlColumn col) {
        fkColumns.add(col);
    }

    MappedColumn createMappedColumn() {
        boolean isUnique = fkSchema.isUnique(fkColumns);
        return new MappedColumn(pkSchema, fkColumns, isUnique);
    }
}
