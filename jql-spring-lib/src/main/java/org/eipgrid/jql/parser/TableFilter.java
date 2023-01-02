package org.eipgrid.jql.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.eipgrid.jql.*;
import org.eipgrid.jql.jdbc.JQResultMapping;

import java.util.*;

class TableFilter extends JqlFilter implements JQResultMapping {
    private final JQSchema schema;

    private final JQJoin join;
    private final String mappingAlias;

    private String[] entityMappingPath;
    private List<JQColumn> selectedColumns = Collections.EMPTY_LIST;
    private boolean doSelectComparedAttribute;
    private boolean isLinear;

    private static final String[] emptyPath = new String[0];

    TableFilter(JQSchema schema, String mappingAlias) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
        this.mappingAlias = mappingAlias;
    }

    TableFilter(TableFilter baseFilter, JQJoin join) {
        super(baseFilter);
        this.schema = join.getAssociatedSchema();
        this.join = join;
        this.mappingAlias = baseFilter.getRootNode().createUniqueMappingAlias();
    }

    public JQSchema getSchema() {
        return schema;
    }

    TableFilter asTableFilter() {
        return this;
    }

    public TableFilter getParentNode() {
        JqlFilter parent = super.getParentNode();
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
    public List<JQColumn> getSelectedColumns() {
        return selectedColumns;
    }


    public void selectProperties(String[] keys) {
        this.selectedColumns = selectProperties_internal(keys,
                this.isArrayNode() || ArrayUtils.contains(keys, JQSelect.PRIMARY_KEYS));
    }

    private List<JQColumn> selectProperties_internal(String[] keys, boolean includePrimaryKeys) {
        if (keys == null || keys.length == 0) {
            return Collections.EMPTY_LIST;
        }

        if (ArrayUtils.contains(keys, JQSelect.ALL_PROPERTIES)) {
            return schema.getReadableColumns();
        }

        if (includePrimaryKeys && keys.length == 1 && JQSelect.PRIMARY_KEYS.equals(keys[0])) {
            return schema.getPKColumns();
        }

        ArrayList<JQColumn> columns = new ArrayList<>(includePrimaryKeys ? schema.getPKColumns() : Collections.EMPTY_LIST);
        for (String name : keys) {
            name = name.trim();
            switch (name) {
                case "@":
                    this.doSelectComparedAttribute = true;
                case "*":
                case "!":
                    break;
                default:
                    JQColumn column = schema.getColumn(name);
                    if (!columns.contains(column))  columns.add(column);
            }
        }
        return columns;
    }

    private Set<JQColumn> getHiddenForeignKeys() {
        Set<JQColumn> hiddenColumns = (Set<JQColumn>) Collections.EMPTY_SET;
        for (JqlFilter node : this.subFilters.values()) {
            TableFilter table = node.asTableFilter();
            if (table == null) continue;
            if (!table.join.isInverseMapped()) {
                if (hiddenColumns == Collections.EMPTY_SET) hiddenColumns = new HashSet<>();
                List<JQColumn> fkColumns = table.join.getForeignKeyColumns();
                assert(fkColumns.get(0).getSchema() == this.schema);
                hiddenColumns.addAll(fkColumns);
            }
        }
        return hiddenColumns;
    }

    @Override
    protected JqlFilter makeSubNode(String key, JqlNodeType nodeType) {
        JQJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            JQColumn column = schema.getColumn(key);
            if (column.getColumnType() != JQType.Object) return this;
        }

        JqlFilter subQuery = subFilters.get(key);
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

    public boolean isEmpty() {
        if (!super.isEmpty()) return false;
        for (JqlFilter subNode: subFilters.values()) {
            if (!subNode.isEmpty()) return false;
        }
        return true;
    }


    @Override
    protected String getColumnName(String key) {
        while (!this.schema.hasColumn(key)) {
            int p = key.indexOf('.');
            if (p < 0) {
                throw new IllegalArgumentException("invalid key: " + key);
            }
            key = key.substring(p + 1);
        }
        return this.schema.getColumn(key).getColumnName();
    }

    public JQJoin getEntityJoin() {
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

    protected void gatherColumnMappings(List<JQResultMapping> columnGroupMappings) {
        columnGroupMappings.add(this);
        this.isLinear = true;
        for (JqlFilter q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                table.gatherColumnMappings(columnGroupMappings);
                this.isLinear &= table.isLinear && !table.isArrayNode();
            }
        }

        Set<JQColumn> hiddenKeys = getHiddenForeignKeys();
        if (!hiddenKeys.isEmpty()) {
            ArrayList<JQColumn> columns = new ArrayList<>();
            for (JQColumn column : this.selectedColumns) {
                if (hiddenKeys.contains(column)) continue;
                columns.add(column);
            }
            this.selectedColumns = columns;
        }
    }



    protected void addComparedAttribute(String key) {
        if (doSelectComparedAttribute) {
            JQColumn column = schema.getColumn(key);
            if (!this.selectedColumns.contains(column)) {
                this.selectedColumns.add(column);
            }
        }
    }
}
