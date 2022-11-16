package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.JqlResultMapping;

import java.util.ArrayList;
import java.util.List;

public class JqlQuery extends TableFilter {

    private final ArrayList<JqlResultMapping> columnGroupMappings = new ArrayList<>();

    public JqlQuery(JqlSchema schema) {
        super(schema);
    }

    public JqlQuery getTopQuery() {
        return this;
    }

    public List<JqlResultMapping> getResultColumnMappings() {
        if (columnGroupMappings.size() == 0) {
            gatherColumnMappings(columnGroupMappings);
        }
        return columnGroupMappings;
    }
}
