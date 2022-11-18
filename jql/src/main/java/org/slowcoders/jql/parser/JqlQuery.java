package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.JqlResultMapping;

import java.util.ArrayList;
import java.util.List;

public class JqlQuery extends TableFilter {

    private final ArrayList<JqlResultMapping> columnGroupMappings = new ArrayList<>();
    private int cntMappingAlias;

    public JqlQuery(JqlSchema schema) {
        super(schema, "t_0");
    }

    public JqlQuery getRootFilter() {
        return this;
    }

    @Override
    public boolean isArrayNode() {
        return false;
    }

    public List<JqlResultMapping> getResultColumnMappings() {
        if (columnGroupMappings.size() == 0) {
            gatherColumnMappings(columnGroupMappings);
        }
        return columnGroupMappings;
    }

    public String createUniqueMappingAlias() {
        cntMappingAlias ++;
        if (cntMappingAlias < 10) {
            return "t_" + cntMappingAlias;
        } else {
            return "t" + cntMappingAlias;
        }
    }
}
