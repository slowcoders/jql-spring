package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QJoin;
import org.eipgrid.jql.schema.QResultMapping;
import org.eipgrid.jql.schema.QSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingNode implements QResultMapping {

    private final MappingNode parent;
    private final String[] entityPath;
    private final String alias;
    private List<QColumn> selectedColumns;
    private boolean hasChildMappings;
    private boolean hasArrayDescendantNode;
    private TableFilter filter;

    private HashMap<String, MappingNode> subFilters = new HashMap<>();

    static int aliasId;

    public MappingNode(TableFilter filter, MappingNode parent) {
        this.filter = filter;
        this.parent = parent;
        this.alias = "t_" + aliasId ++;
        if (parent == null) {
            entityPath = new String[0];
        }
        else {
            String[] basePath = parent.entityPath;
            this.entityPath = new String[basePath.length + 1];
            System.arraycopy(basePath, 0, entityPath, 0, basePath.length);
            entityPath[basePath.length] = filter.getEntityJoin().getJsonKey();
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

    public void resolveMapping(ArrayList<QResultMapping> mappings, JqlSelect.ResultMap selection) {
        QSchema schema = filter.getSchema();
        if (filter.isArrayNode()) {
            for (MappingNode mapping = this.parent; mapping != null && !mapping.filter.isArrayNode(); ) {
                mapping.hasArrayDescendantNode = true;
            }
        }

        if (selection.containsKey("*")) {
            selectedColumns = schema.getLeafColumns();
        } else if (filter.isArrayNode() || selection.containsKey("0")) {
            selectedColumns = schema.getPKColumns();
        } else {
            selectedColumns = new ArrayList<>();
        }

        for (Map.Entry<String, JqlSelect.ResultMap> entry : selection.entrySet()) {
            String key = entry.getKey();
            EntityFilter subFilter = filter.getFilterNode(key, JqlParser.NodeType.Entity);
            if (subFilter != null && subFilter.asTableFilter() != null) {
                MappingNode subMap = new MappingNode(subFilter.asTableFilter(), this);
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
        return filter.getSchema();
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
        return filter.getEntityJoin();
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
        return filter.isArrayNode();
    }

    @Override
    public boolean hasArrayDescendantNode() {
        return this.hasArrayDescendantNode;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public static List<QResultMapping> resolveResultMappings(JqlFilter filter, JqlSelect select) {
        filter.setSelectedProperties(select.getPropertyMap());
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
