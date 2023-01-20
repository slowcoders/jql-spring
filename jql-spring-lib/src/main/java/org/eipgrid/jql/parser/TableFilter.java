package org.eipgrid.jql.parser;

import org.eipgrid.jql.schema.QResultMapping;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QJoin;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.schema.QType;

import java.util.*;

class TableFilter extends EntityFilter implements QResultMapping {
    private final QSchema schema;

    private final QJoin join;
    private final String mappingAlias;

    private String[] entityMappingPath;
    private List<QColumn> selectedColumns = null;
    private boolean hasArrayDescendant;
    private int selectAliases;

    private static final String[] emptyPath = new String[0];

    TableFilter(QSchema schema, String mappingAlias) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
        this.mappingAlias = mappingAlias;
    }

    TableFilter(TableFilter baseFilter, QJoin join) {
        super(baseFilter);
        this.schema = join.getAssociatedSchema();
        this.join = join;
        this.mappingAlias = baseFilter.getRootNode().createUniqueMappingAlias();
    }

    public QSchema getSchema() {
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
            boolean mergeLast = false && (parent.getSelectedColumns().isEmpty()) && basePath.length > 0;
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


    public String getTableName() {
        return schema.getTableName();
    }

    @Override
    public List<QColumn> getSelectedColumns() {
        if (selectedColumns == null) {
            if (!getRootNode().isSelectAuto()) {
                this.selectedColumns = Collections.EMPTY_LIST;
            }
            else {
                this.selectedColumns = schema.getPrimitiveColumns();
//
//                Set<QColumn> hiddenKeys = this.getHiddenForeignKeys();
//                if (!hiddenKeys.isEmpty()) {
//                    ArrayList<QColumn> columns2 = new ArrayList<>();
//                    for (QColumn column : selectedColumns) {
//                        if (hiddenKeys.contains(column)) continue;
//                        columns2.add(column);
//                    }
//                    selectedColumns = columns2;
//                }
            }
        }
        return selectedColumns;
    }


    Set<QColumn> getHiddenForeignKeys() {
        Set<QColumn> hiddenColumns = (Set<QColumn>) Collections.EMPTY_SET;
        for (EntityFilter node : this.subFilters.values()) {
            TableFilter table = node.asTableFilter();
            if (table == null) continue;
            if (!table.join.isInverseMapped()) {
                if (hiddenColumns == Collections.EMPTY_SET) hiddenColumns = new HashSet<>();
                List<QColumn> fkColumns = table.join.getForeignKeyColumns();
                assert (fkColumns.get(0).getSchema() == this.schema);
                hiddenColumns.addAll(fkColumns);
            }
        }
        return hiddenColumns;
    }

    @Override
    protected EntityFilter makeSubNode(String key, JqlParser.NodeType nodeType) {
        QJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            if (schema.getColumn(key).getType() != QType.Json) return this;
        }

        EntityFilter subQuery = subFilters.get(key);
        if (subQuery == null) {
            if (join != null) {
                subQuery = new TableFilter(this, join);
            } else {
                subQuery = new JsonFilter(this, key);
            }
            subFilters.put(key, subQuery);
        }
        return subQuery;
    }

    final void addSelectedColumn(String key) {
        if (key.equals("*")) {
            this.selectedColumns = schema.getPrimitiveColumns();
        }
        else if (key.equals("0")) {
            if (this.selectedColumns == null) {
                this.selectedColumns = schema.getPKColumns();
            }
            else {
                for (QColumn k : schema.getPKColumns()) {
                    addSelectedColumn(k);
                }
            }
        }
        else {
            QColumn column = schema.getColumn(key);
            addSelectedColumn(column);
        }
    }

    private boolean mustSelectPKs() {
        return this.isArrayNode() && !this.subFilters.isEmpty();
    }

    private void addSelectedColumn(QColumn column) {
        if (this.selectedColumns == null) {
            this.selectedColumns = mustSelectPKs() ? new ArrayList<>(schema.getPKColumns()) : new ArrayList<>();
        }

        if (this.selectedColumns.contains(column)) return;

        if (!(this.selectedColumns instanceof ArrayList)) {
            // makes mutable!!
            this.selectedColumns = new ArrayList<>(this.selectedColumns);
        }
        this.selectedColumns.add(column);
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

    public QJoin getEntityJoin() {
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

    protected void gatherColumnMappings(List<QResultMapping> columnGroupMappings) {
        columnGroupMappings.add(this);
        this.hasArrayDescendant = false;
        for (EntityFilter q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                table.gatherColumnMappings(columnGroupMappings);
                this.hasArrayDescendant |= table.isArrayNode() || table.hasArrayDescendant;
            }
        }
    }

    protected void addComparedPropertyToSelection(String key) {
        if ((selectAliases & KeySet.Auto.bit()) != 0) {
            QColumn column = schema.getColumn(key);
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
