package org.slowcoders.hyperql.parser;

import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;

import java.util.ArrayList;
import java.util.Map;

class JsonFilter extends EntityFilter {
    private final String key;

    JsonFilter(EntityFilter parentQuery, String key) {
        super(parentQuery);
        this.key = key;
    }

    @Override
    public String getMappingAlias() {
        return this.key;
    }

    @Override
    public EntityFilter makeSubNode(String key, HqlParser.NodeType nodeType) {
        if (nodeType == HqlParser.NodeType.Leaf) {
            return this;
        }
        EntityFilter entity = subFilters.get(key);
        if (entity == null) {
            entity = new JsonFilter(this, key);
            subFilters.put(key, entity);
        }
        return entity;
    }


    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private TableFilter getTableFilter() {
        EntityFilter f = this;
        while (true) {
            var table = f.asTableFilter();
            if (table != null) {
                return table;
            }
            f = f.getParentNode();
        }
    }
    protected void addSelection(HyperSelect.ResultMap resultMap) {

        for (Map.Entry<String, HyperSelect.ResultMap> entry : resultMap.entrySet()) {
            HyperSelect.ResultMap subMap = entry.getValue();
            if (subMap.isEmpty()) {
                var selectedColumns = getTableFilter().getSelectedColumns();
                selectedColumns.add(new JsonColumn(this, entry.getKey(), null));
            } else {
                EntityFilter scope = this.makeSubNode(key, HqlParser.NodeType.Entity);
                scope.addSelection(subMap);
            }
        }
    }

}
