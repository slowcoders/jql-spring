package org.slowcoders.hyperql.parser;

import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;

public class JsonColumn extends QColumn {
    private final EntityFilter filter;

    public JsonColumn(EntityFilter filter, String name, Class type) {
        super(name, type);
        this.filter = filter;
    }

    @Override
    public QSchema getSchema() { return null; }

    @Override
    public String getJsonKey() {
        return super.getPhysicalName();
    }

    public EntityFilter getEntityNode() {
        return filter;
    }
}
