package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QJoin;
import org.eipgrid.jql.schema.QResultMapping;
import org.eipgrid.jql.schema.QSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SelectionMap extends HashMap<String, SelectionMap> implements QResultMapping {

    private final QSchema schema;
    private final SelectionMap parent;
    private final QJoin join;
    private final String[] entityPath;
    private final String alias;
    private List<QColumn> selectedColumns;
    private boolean hasChildMappings;
    private boolean isArrayNode;
    private boolean hasArrayDescendantNode;


    public SelectionMap(QSchema schema, SelectionMap parent, QJoin join, int aliasId) {
        this.schema = schema;
        this.parent = parent;
        this.join = join;
        this.alias = "t_" + aliasId;
        if (parent == null) {
            entityPath = new String[0];
        }
        else {
            String[] basePath = parent.entityPath;
            this.entityPath = new String[basePath.length + 1];
            System.arraycopy(basePath, 0, entityPath, 0, basePath.length);
            entityPath[basePath.length] = join.getJsonKey();
        }
    }

    public void addSelectedColumn(QColumn column) {
        if (this.selectedColumns.contains(column)) return;

        if (!(this.selectedColumns instanceof ArrayList)) {
            // makes mutable!!
            this.selectedColumns = new ArrayList<>(this.selectedColumns);
        }
        this.selectedColumns.add(column);
    }

    public void resolveMapping(ArrayList<QResultMapping> mappings, JqlSelect.PropertyMap selection) {
        this.isArrayNode = join == null || !join.hasUniqueTarget();
        if (this.isArrayNode) {
            for (SelectionMap mapping = this.parent; mapping != null && !mapping.isArrayNode; ) {
                mapping.hasArrayDescendantNode = true;
            }
        }

        if (selection.containsKey("*")) {
            selectedColumns = schema.getLeafColumns();
        } else if (isArrayNode || selection.containsKey("0")) {
            selectedColumns = schema.getPKColumns();
        } else {
            selectedColumns = new ArrayList<>();
        }

        for (Map.Entry<String, JqlSelect.PropertyMap> entry : selection.entrySet()) {
            String key = entry.getKey();
            QJoin join = schema.getEntityJoinBy(key);
            if (join != null) {
                SelectionMap subMap = new SelectionMap(join.getTargetSchema(), this, join, mappings.size() + 1);
                mappings.add(subMap);
                subMap.resolveMapping(mappings, entry.getValue());
                this.hasChildMappings = true;
            } else {
                QColumn column = schema.findColumn(key);
                if (column != null) {
                    this.addSelectedColumn(column);
                }
            }
        }
    }

    @Override
    public QSchema getSchema() {
        return this.schema;
    }

    @Override
    public QResultMapping getParentNode() {
        return parent;
    }

    @Override
    public boolean hasChildMappings() {
        return this.hasChildMappings;
    }

    @Override
    public String getMappingAlias() {
        return this.alias;
    }

    @Override
    public QJoin getEntityJoin() {
        return this.join;
    }

    @Override
    public List<QColumn> getSelectedColumns() {
        return this.selectedColumns;
    }

    @Override
    public String[] getEntityMappingPath() {
        return this.entityPath;
    }

    @Override
    public boolean isArrayNode() {
        return isArrayNode;
    }

    @Override
    public boolean hasArrayDescendantNode() {
        return this.hasArrayDescendantNode;
    }

    public static List<QResultMapping> resolveResultMappings(JqlFilter filter, JqlSelect select) {
        filter.setSelectedProperties(select.getPropertyNames());
        return filter.getResultMappings();
//        JqlSelect.PropertyMap selectMap;
//        if (select == null || select == JqlSelect.Auto) {
//            selectMap = filter.getSelectionMap();
//        }
//        else {
//            selectMap = select.getPropertyMap();
//        }
//
//        ArrayList<QResultMapping> mappings = new ArrayList<>();
//        SelectionMap map = new SelectionMap(filter.getSchema(), null, null, 0);
//        mappings.add(map);
//        map.resolveMapping(mappings, selectMap);
//        return mappings;
    }
}
