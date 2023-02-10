package org.eipgrid.jql.parser;

import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QType;

public class JsonColumn extends QColumn {
    protected JsonColumn(String name, Class type) {
        super(null, name, type);
    }

    @Override
    public String getJsonKey() {
        return super.getStoredName();
    }
}
