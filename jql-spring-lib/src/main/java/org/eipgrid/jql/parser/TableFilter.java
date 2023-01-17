package org.eipgrid.jql.parser;

import org.eipgrid.jql.jdbc.JQResultMapping;
import org.eipgrid.jql.schema.JQColumn;
import org.eipgrid.jql.schema.JQJoin;
import org.eipgrid.jql.schema.JQSchema;
import org.eipgrid.jql.schema.JQType;

import java.util.*;

class TableFilter extends EntityFilter implements JQResultMapping {
    private final JQSchema schema;

    private final JQJoin join;
    private final String mappingAlias;

    private String[] entityMappingPath;
    private List<JQColumn> selectedColumns = null;
    private boolean hasArrayDescendant;
    private int selectAliases;

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
        EntityFilter parent = super.getParentNode();
        return parent == null ? null : parent.asTableFilter();
    }

    @Override
    public String getMappingAlias() {
        return mappingAlias;
    }

    public String[] getEntityMappingPath() {
        String[] jsonPath = this.entityMappingPath;
        if (jsonPath == null) {
            TableFilter parent = getParentNode();
            String[] basePath = parent.getEntityMappingPath();
            boolean mergeLast = (parent.selectedColumns == null) && basePath.length > 0;
            jsonPath = new String[basePath.length + (mergeLast ? 0 : 1)];
            System.arraycopy(basePath, 0, jsonPath, 0, basePath.length);
            String lastPath = join.getJsonKey();
            if (mergeLast) {
                lastPath = basePath[basePath.length - 1] + "." + lastPath;
            }
            jsonPath[jsonPath.length - 1] = lastPath;
            this.entityMappingPath = jsonPath;
        }
        return jsonPath;
    }


    public String getTableName() { return schema.getTableName(); }

    @Override
    public List<JQColumn> getSelectedColumns() {
        return selectedColumns == null ? Collections.EMPTY_LIST : selectedColumns;
    }


    public void setSelectedProperties(String[] keys) {
        if (keys == null) {
            if (this.selectedColumns == null) {
                TableFilter parent = getParentNode();
                if (parent == null) {
                    this.selectedColumns = schema.getReadableColumns();
                    this.selectAliases = KeySet.All.bit();
                } else {
                    while (parent.selectedColumns == null) {
                        parent = parent.getParentNode();
                    }
                    this.selectAliases = parent.selectAliases;
                    this.selectedColumns = resolveSelectedColumns(null, false);
                }
            }
            return;
        }

        if (keys.length == 0) {
            this.selectedColumns = Collections.EMPTY_LIST;
        } else {
            boolean hasAdditionalKey = false;
            for (String k : keys) {
                KeySet keySet = KeySet.toAlias(k);
                if (keySet != null) {
                    selectAliases |= 1 << keySet.ordinal();
                }
                else {
                    hasAdditionalKey = true;
                }
            }

            this.selectedColumns = resolveSelectedColumns(keys, hasAdditionalKey);
        }
    }

    protected void setSelectedProperties_withEmptyFilter() {
        if (this.selectedColumns == null || selectedColumns.size() == 0) {
            this.selectedColumns = schema.getPKColumns();
        }
    }

    private List<JQColumn> resolveSelectedColumns(String[] keys, boolean hasAdditionalKey) {
        if ((selectAliases & KeySet.All.bit()) != 0) {
            return schema.getReadableColumns();
        }

        boolean explicitPKs = (selectAliases & KeySet.PrimaryKeys.bit()) != 0;
        boolean doSelectComparedAttribute = (selectAliases & KeySet.Auto.bit()) != 0;

        boolean includePKs = explicitPKs || (doSelectComparedAttribute && this.isArrayNode());
        List<JQColumn> baseColumns = includePKs ? schema.getPKColumns() : Collections.EMPTY_LIST;

        if (selectedColumns == null && !doSelectComparedAttribute && !hasAdditionalKey) {
            return baseColumns;
        }

        List<JQColumn> columns = new ArrayList<>(baseColumns);
        if (selectedColumns != null) {
            for (JQColumn column : selectedColumns) {
                if (!columns.contains(column)) columns.add(column);
            }
        }

        if (hasAdditionalKey) {
            for (String k : keys) {
                KeySet keySet = KeySet.toAlias(k);
                if (keySet == null) {
                    JQColumn column = schema.getColumn(k);
                    if (!columns.contains(column)) columns.add(column);
                }
            }
        }
        return columns;
    }

    private Set<JQColumn> getHiddenForeignKeys() {
        Set<JQColumn> hiddenColumns = (Set<JQColumn>) Collections.EMPTY_SET;
        for (EntityFilter node : this.subFilters.values()) {
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
    protected EntityFilter makeSubNode(String key, JqlParser.NodeType nodeType) {
        JQJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            JQColumn column = schema.getColumn(key);
            if (column.getType() != JQType.Json) return this;
        }

        EntityFilter subQuery = subFilters.get(key);
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

    @Override
    protected String getColumnName(String key) {
        while (!this.schema.hasColumn(key)) {
            int p = key.indexOf('.');
            if (p < 0) {
                throw new IllegalArgumentException("invalid key: " + key);
            }
            key = key.substring(p + 1);
        }
        return this.schema.getColumn(key).getPhysicalName();
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
        for (EntityFilter q : subFilters.values()) {
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
        if ((selectAliases & KeySet.Auto.bit()) != 0) {
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
