package org.eipgrid.jql.parser;

import org.eipgrid.jql.*;
import org.eipgrid.jql.jdbc.JQResultMapping;

import java.util.*;

class TableFilter extends JqlFilter implements JQResultMapping {
    private final JQSchema schema;

    private final JQJoin join;
    private final String mappingAlias;

    private String[] entityMappingPath;
    private List<JQColumn> selectedColumns = null;
    private boolean doSelectComparedAttribute;
    private boolean hasArrayDescendant;

//    private KeySet joinedPropertyDefaultSelection;
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
//        if (selectedColumns == null && getParentNode() != null) {
//            String[] keys = getParentNode().getDefaultJoinedPropertySelection().asKeyArray();
//            selectedColumns = selectProperties_internal(keys);
//        }
        return selectedColumns == null ? Collections.EMPTY_LIST : selectedColumns;
    }

//    private KeySet getDefaultJoinedPropertySelection() {
//        KeySet keySet = joinedPropertyDefaultSelection;
//        if (keySet == null) {
//            TableFilter parent = this.getParentNode();
//            keySet = parent == null ? KeySet.Auto : parent.getDefaultJoinedPropertySelection();
//        }
//        return keySet;
//    }


    public void setSelectedProperties(String[] keys) {
        if (keys == null) {
            TableFilter parent = getParentNode();
            this.doSelectComparedAttribute = parent == null || parent.selectedColumns != Collections.EMPTY_LIST;
            this.selectedColumns = doSelectComparedAttribute ? new ArrayList<>() : Collections.EMPTY_LIST;
            return;
        }

        this.selectedColumns = selectProperties_internal(keys);
    }

    protected void setSelectedProperties_withEmptyFilter() {
        if (this.selectedColumns == null || selectedColumns.size() == 0) {
            this.selectedColumns = schema.getPKColumns();
        }
    }

    private List<JQColumn> selectProperties_internal(String[] keys) {
        if (keys.length == 0) return Collections.EMPTY_LIST;

        boolean hasAdditionalKey = false;
        int keyBits = 0;
        for (String k : keys) {
            KeySet keySet = KeySet.toAlias(k.charAt(0));
            if (keySet == null) {
                hasAdditionalKey = true;
                continue;
            }
            keyBits |= 1 << keySet.ordinal();
//            if (k.length() == 1) continue;
//            if (k.length() == 3 && k.charAt(1) == '.' && joinedPropertyDefaultSelection == null) {
//                joinedPropertyDefaultSelection = KeySet.toAlias(k.charAt(2));
//            }
//            else {
//                throw new RuntimeException("Joined entity selections are conflicted");
//            }
        }

        if ((keyBits & KeySet.All.bit()) != 0) {
            return schema.getReadableColumns();
        }

        boolean explicitPKs = (keyBits & KeySet.PrimaryKeys.bit()) != 0;
        this.doSelectComparedAttribute = (keyBits & KeySet.Auto.bit()) != 0;

        boolean includePKs = explicitPKs || (doSelectComparedAttribute && this.isArrayNode() && this.hasArrayDescendantNode());
        List<JQColumn> baseColumns = includePKs ? schema.getPKColumns() : Collections.EMPTY_LIST;

        if (!doSelectComparedAttribute && !hasAdditionalKey) {
            return baseColumns;
        }

        List<JQColumn> columns = new ArrayList<>(baseColumns);

        for (String k : keys) {
            KeySet keySet = KeySet.toAlias(k.charAt(0));
            if (keySet == null) {
                JQColumn column = schema.getColumn(k);
                if (!columns.contains(column)) columns.add(column);
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
    public boolean hasArrayDescendantNode() {
        return this.hasArrayDescendant;
    }

    protected void gatherColumnMappings(List<JQResultMapping> columnGroupMappings) {
        columnGroupMappings.add(this);
        this.hasArrayDescendant = false;
        for (JqlFilter q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                table.gatherColumnMappings(columnGroupMappings);
                this.hasArrayDescendant |= table.isArrayNode() || table.hasArrayDescendant;
            }
        }

        if (this.selectedColumns == null || selectedColumns.size() == 0) return;

        Set<JQColumn> hiddenKeys = getHiddenForeignKeys();
        if (!hiddenKeys.isEmpty()) {
            ArrayList<JQColumn> columns = new ArrayList<>();
            for (JQColumn column : this.getSelectedColumns()) {
                if (hiddenKeys.contains(column)) continue;
                columns.add(column);
            }
            this.selectedColumns = columns;
        }
    }

    protected void addComparedPropertyToSelection(String key) {
        if (doSelectComparedAttribute) {
            JQColumn column = schema.getColumn(key);
            if (!this.selectedColumns.contains(column)) {
                this.selectedColumns.add(column);
            }
        }
    }

    @Override
    public String toString() {
        return join != null ? join.getJsonKey() : schema.getTableName();
    }
}
