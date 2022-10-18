package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

import java.util.Collections;
import java.util.List;

public class MappedColumn extends JqlColumn {
    private final String jsonName;
    private List<JqlColumn> mappedExternalColumns;

    public MappedColumn(JqlSchema pkSchema, String jsonName, List<JqlColumn> fkColumns, boolean isUnique) {
        super(pkSchema, jsonName, Object.class, isUnique ? JsonNodeType.Object : JsonNodeType.Array);
        this.mappedExternalColumns = Collections.unmodifiableList(fkColumns);
        this.jsonName = jsonName;
    }

    @Override
    public String getJsonName() {
        return jsonName;
    }

    public boolean isReadOnly() {
        return true;
    }

    public List<JqlColumn> getMappedExternalColumns() {
        return this.mappedExternalColumns;
    }
}
