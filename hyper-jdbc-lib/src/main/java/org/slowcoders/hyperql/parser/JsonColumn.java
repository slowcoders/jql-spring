package org.slowcoders.hyperql.parser;

import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;

public class JsonColumn extends QColumn {
    protected JsonColumn(String name, Class type) {
        super(name, type);
    }

    @Override
    public QSchema getSchema() { return null; }

    @Override
    public String getJsonKey() {
        return super.getPhysicalName();
    }
}
