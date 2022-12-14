package org.eipgrid.jql.parser;

import org.eipgrid.jql.*;
import org.eipgrid.jql.jdbc.JqlResultMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class TableFilter extends JqlNode implements JqlResultMapping {
    private final JqlSchema schema;

    private final JqlSchemaJoin join;
    private final String mappingAlias;
    private final HashMap<String, JqlNode> subFilters = new HashMap<>();

    private String[] entityMappingPath;
    private List<JqlColumn> selectedColumns = Collections.EMPTY_LIST;
    private boolean isLinear;

    private static final String[] emptyPath = new String[0];

    TableFilter(JqlSchema schema, String mappingAlias) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
        this.mappingAlias = mappingAlias;
    }

    TableFilter(TableFilter baseFilter, JqlSchemaJoin join) {
        super(baseFilter);
        this.schema = join.getAssociatedSchema();
        this.join = join;
        this.mappingAlias = baseFilter.getRootNode().createUniqueMappingAlias();
    }

    public JqlSchema getSchema() {
        return schema;
    }

    TableFilter asTableFilter() {
        return this;
    }

    public TableFilter getParentNode() {
        JqlNode parent = super.getParentNode();
        return parent == null ? null : parent.asTableFilter();
    }

    @Override
    public String getMappingAlias() {
        return mappingAlias;
    }

    public String[] getEntityMappingPath() {
        String[] jsonPath = this.entityMappingPath;
        if (jsonPath == null) {
            String[] basePath = getParentNode().getEntityMappingPath();
            jsonPath = new String[basePath.length + 1];
            System.arraycopy(basePath, 0, jsonPath, 0, basePath.length);
            jsonPath[basePath.length] = join.getJsonKey();
            this.entityMappingPath = jsonPath;
        }
        return jsonPath;
    }


    public String getTableName() { return schema.getTableName(); }

    @Override
    public List<JqlColumn> getSelectedColumns() {
        return selectedColumns;
    }

    public void setSelectedColumns(JqlSelect select) {
        this.selectedColumns = select.getSelectedColumns(schema);
    }

    public TableFilter getTableFilter() {
        return this;
    }

    @Override
    public JqlNode makeSubNode(String key, ValueNodeType nodeType) {
        JqlSchemaJoin join = schema.getSchemaJoinBy(key);
        if (join == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != JsonNodeType.Object) return this;
        }

        JqlNode subQuery = subFilters.get(key);
        if (subQuery == null) {
            if (join != null) {
                subQuery = new TableFilter(this, join);
            }
            else {
                subQuery = new JsonFilter(this, key);
            }
            subFilters.put(key, subQuery);
        }
        return subQuery;
    }

    public boolean hasFilterPredicates() {
        return !this.isEmpty();
    }


    @Override
    public String getColumnName(String key) {
        while (!this.schema.hasColumn(key)) {
            int p = key.indexOf('.');
            if (p < 0) {
                throw new IllegalArgumentException("invalid key: " + key);
            }
            key = key.substring(p + 1);
        }
        return this.schema.getColumn(key).getColumnName();
    }

    public JqlSchemaJoin getSchemaJoin() {
        return this.join;
    }

    @Override
    public boolean isArrayNode() {
        return !this.join.isUniqueJoin();
    }

    @Override
    public boolean isLinearNode() {
        return this.isLinear;
    }

    protected void gatherColumnMappings(List<JqlResultMapping> columnGroupMappings) {
        columnGroupMappings.add(this);
        this.isLinear = true;
        for (JqlNode q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                table.gatherColumnMappings(columnGroupMappings);
                this.isLinear &= table.isLinear && !table.isArrayNode();
            }
        }
        if (!this.isLinear && this.getSelectedColumns() != schema.getReadableColumns()) {
            if (selectedColumns == Collections.EMPTY_LIST) {
                selectedColumns = new ArrayList<>();
            }
            List<JqlColumn> pkColumns = schema.getPKColumns();
            for (int idxPk = pkColumns.size(); --idxPk >= 0; ) {
                JqlColumn pk = pkColumns.get(idxPk);
                int i = this.selectedColumns.indexOf(pk);
                if (i == idxPk) continue;
                if (i >= 0) {
                    JqlColumn col = this.selectedColumns.get(idxPk);
                    this.selectedColumns.set(i, col);
                    this.selectedColumns.set(idxPk, pk);
                } else {
                    this.selectedColumns.add(idxPk, pk);
                }
            }
        }
    }

}
