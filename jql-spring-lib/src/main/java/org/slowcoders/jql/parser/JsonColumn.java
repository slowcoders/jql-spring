package org.slowcoders.jql.parser;

import org.slowcoders.jql.schema.QColumn;
import org.slowcoders.jql.schema.QSchema;

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
