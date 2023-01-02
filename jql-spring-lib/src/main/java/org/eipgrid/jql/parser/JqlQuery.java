package org.eipgrid.jql.parser;

import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.jdbc.JQResultMapping;

import java.util.ArrayList;
import java.util.List;

public class JqlQuery extends TableFilter {

    private final ArrayList<JQResultMapping> columnGroupMappings = new ArrayList<>();
    private int cntMappingAlias;

    public JqlQuery(JQSchema schema) {
        super(schema, "t_0");
    }

    public JqlQuery getRootNode() {
        return this;
    }

    @Override
    public boolean isArrayNode() {
        return true;
    }

    public List<JQResultMapping> getResultMappings() {
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
