package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.jdbc.JqlResultMapping;

import java.util.ArrayList;
import java.util.List;

public class JqlQuery extends TableFilter {

    private final ArrayList<JqlResultMapping> columnGroupMappings = new ArrayList<>();
    private int cntMappingAlias;

    public JqlQuery(JqlSchema schema) {
        super(schema, "t_0");
    }

    public JqlQuery getRootNode() {
        return this;
    }

    @Override
    public boolean isArrayNode() {
        return true;
    }

    public List<JqlResultMapping> getResultMappings() {
        if (columnGroupMappings.size() == 0) {
            gatherColumnMappings(columnGroupMappings);
        }
        return columnGroupMappings;
    }

    String createUniqueMappingAlias() {
        cntMappingAlias ++;
        if (cntMappingAlias < 10) {
            return "t_" + cntMappingAlias;
        } else {
            return "t" + cntMappingAlias;
        }
    }
}
