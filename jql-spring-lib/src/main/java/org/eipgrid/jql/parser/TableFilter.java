package org.eipgrid.jql.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.eipgrid.jql.*;
import org.eipgrid.jql.jdbc.JqlResultMapping;

import java.util.*;

class TableFilter extends JqlNode implements JqlResultMapping {
    private final JqlSchema schema;

    private final JqlEntityJoin join;
    private final String mappingAlias;

    private String[] entityMappingPath;
    private List<JqlColumn> selectedColumns = Collections.EMPTY_LIST;
    private boolean doSelectComparedAttribute;
    private boolean isLinear;

    private static final String[] emptyPath = new String[0];

    TableFilter(JqlSchema schema, String mappingAlias) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
        this.mappingAlias = mappingAlias;
    }

    TableFilter(TableFilter baseFilter, JqlEntityJoin join) {
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


    public void selectProperties(String[] keys) {
        this.selectedColumns = selectProperties_internal(keys,
                this.isArrayNode() || ArrayUtils.contains(keys, JqlSelect.PRIMARY_KEYS));
    }

    private List<JqlColumn> selectProperties_internal(String[] keys, boolean includePrimaryKeys) {
        if (keys == null || keys.length == 0) {
            return Collections.EMPTY_LIST;
        }

        if (ArrayUtils.contains(keys, JqlSelect.ALL_PROPERTIES)) {
            return schema.getReadableColumns();
        }

        if (includePrimaryKeys && keys.length == 1 && JqlSelect.PRIMARY_KEYS.equals(keys[0])) {
            return schema.getPKColumns();
        }

        ArrayList<JqlColumn> columns = new ArrayList<>(includePrimaryKeys ? schema.getPKColumns() : Collections.EMPTY_LIST);
        for (String name : keys) {
            name = name.trim();
            switch (name) {
                case "@":
                    this.doSelectComparedAttribute = true;
                case "*":
                case "!":
                    break;
                default:
                    JqlColumn column = schema.getColumn(name);
                    if (!columns.contains(column))  columns.add(column);
            }
        }
        return columns;
    }

    private Set<JqlColumn> getHiddenForeignKeys() {
        Set<JqlColumn> hiddenColumns = (Set<JqlColumn>) Collections.EMPTY_SET;
        for (JqlNode node : this.subFilters.values()) {
            TableFilter table = node.asTableFilter();
            if (table == null) continue;
            if (!table.join.isInverseMapped()) {
                if (hiddenColumns == Collections.EMPTY_SET) hiddenColumns = new HashSet<>();
                List<JqlColumn> fkColumns = table.join.getForeignKeyColumns();
                assert(fkColumns.get(0).getSchema() == this.schema);
                hiddenColumns.addAll(fkColumns);
            }
        }
        return hiddenColumns;
    }

    @Override
    protected JqlNode makeSubNode(String key, ValueNodeType nodeType) {
        JqlEntityJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueKind() != JqlValueKind.Object) return this;
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

    public boolean isEmpty() {
        if (!super.isEmpty()) return false;
        for (JqlNode subNode: subFilters.values()) {
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

    public JqlEntityJoin getEntityJoin() {
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

        Set<JqlColumn> hiddenKeys = getHiddenForeignKeys();
        if (!hiddenKeys.isEmpty()) {
            ArrayList<JqlColumn> columns = new ArrayList<>();
            for (JqlColumn column : this.selectedColumns) {
                if (hiddenKeys.contains(column)) continue;
                columns.add(column);
            }
            this.selectedColumns = columns;
        }
    }



    protected void addComparedAttribute(String key) {
        if (doSelectComparedAttribute) {
            JqlColumn column = schema.getColumn(key);
            if (!this.selectedColumns.contains(column)) {
                this.selectedColumns.add(column);
            }
        }
    }
}
