package org.slowcoders.hyperql.parser;

import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.schema.EntityAccessGuard;
import org.slowcoders.hyperql.jdbc.storage.SqlGenerator;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QJoin;
import org.slowcoders.hyperql.schema.QResultMapping;
import org.slowcoders.hyperql.schema.QSchema;
import org.springframework.data.domain.Sort;

import java.util.*;

public class TableFilter extends EntityFilter implements QResultMapping {
    private final QSchema schema;
    private final QJoin join;
    private final String mappingAlias;

    private String[] entityMappingPath;
    private List<QColumn> selectedColumns = null;
    private List<Sort.Order> orders = new ArrayList<>();
    private boolean hasArrayDescendant;

    private static final String[] emptyPath = new String[0];
    private boolean hasJoinedChildMapping;

    TableFilter(QSchema schema, String mappingAlias) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
        this.mappingAlias = mappingAlias;
    }

    TableFilter(TableFilter baseFilter, QJoin join) {
        super(baseFilter);
        this.schema = join.getTargetSchema();
        this.join = join;
        this.mappingAlias = baseFilter.getRootNode().createUniqueMappingAlias();
    }

    public QSchema getSchema() {
        return schema;
    }

    public TableFilter asTableFilter() {
        return this;
    }

    public TableFilter getParentNode() {
        EntityFilter parent = super.getParentNode();
        return parent == null ? null : parent.asTableFilter();
    }

    public boolean hasChildMappings() { return !subFilters.isEmpty(); }

    public boolean hasJoinedChildMapping() { return this.hasJoinedChildMapping; }

    @Override
    public String getMappingAlias() {
        return mappingAlias;
    }

    public String[] getEntityMappingPath() {
        String[] jsonPath = this.entityMappingPath;
        if (jsonPath == null) {
            TableFilter parent = getParentNode();
            String[] basePath = parent.getEntityMappingPath();
            jsonPath = new String[basePath.length + 1];
            System.arraycopy(basePath, 0, jsonPath, 0, basePath.length);
            String lastPath = join.getJsonKey();
            jsonPath[jsonPath.length - 1] = lastPath;
            this.entityMappingPath = jsonPath;
        }
        return jsonPath;
    }


    public String getTableExpression(String[] params) {
        return schema.getTableExpression(params);
    }

    @Override
    public List<QColumn> getSelectedColumns() {
        if (selectedColumns == null) {
            if (SqlGenerator.JSON_RS || !getRootNode().isSelectAuto()) {
                this.selectedColumns = new ArrayList<>();
            }
            else {
                this.selectedColumns = schema.getBaseColumns();
                if (!SqlGenerator.JSON_RS && this.schema.getExtendedColumns().size() > 0) {
                    for (Map.Entry<String, EntityFilter> entry : this.subFilters.entrySet()) {
                        EntityFilter filter = entry.getValue();
                        if (filter.isJsonNode()) {
                            addSelectedColumn(entry.getKey());
                        }
                    }
                }
            }
        }
        return selectedColumns;
    }

    @Override
    protected EntityFilter makeSubNode(String key, HqlParser.NodeType nodeType) {
        QJoin join = schema.getEntityJoinBy(key);
        QColumn jsonColumn = null;
        if (join == null) {
            jsonColumn = schema.getColumn(key);
            if (!jsonColumn.isJsonNode() || nodeType == HqlParser.NodeType.Leaf) return this;
        }

        EntityFilter subQuery = subFilters.get(key);
        if (subQuery == null) {
            if (join != null) {
                subQuery = new TableFilter(this, join);
                this.hasJoinedChildMapping = true;
            } else {
                subQuery = new JsonFilter(this, jsonColumn.getPhysicalName());
                if (!SqlGenerator.JSON_RS) {
                    this.addSelectedColumn(jsonColumn);
                }
            }
            subFilters.put(key, subQuery);
        }
        return subQuery;
    }

    protected void addSelectedColumn(String key) {
        if (key.length() == 1) {
            List<QColumn> columns;
            switch (key.charAt(0)) {
                case HyperSelect.LeafProperties ->
                        columns = schema.getBaseColumns();
                case HyperSelect.PrimaryKeys ->
                        columns = schema.getPKColumns();
                default ->
                        columns = null;
            }
            if (columns != null) {
                for (var col : columns) {
                    this.addSelectedColumn(col);
                }
                return;
            }
        }
        this.addSelectedColumn(this.schema.getColumn(key));
    }

    private void addSelectedColumn(QColumn column) {
        if (this.selectedColumns == null) {
            this.selectedColumns = isArrayNode() && hasJoinedChildMapping() ? new ArrayList<>(schema.getPKColumns()) : new ArrayList<>();
        }

        if (this.selectedColumns.contains(column)) return;

        if (!(this.selectedColumns instanceof ArrayList)) {
            // makes mutable!!
            this.selectedColumns = new ArrayList<>(this.selectedColumns);
        }
        this.selectedColumns.add(column);
    }

    public String getSqlToCheckReadable() {
        EntityAccessGuard acl = this.schema.getAccessGuard();
        if (acl == null) return null;
        return acl.checkReadable(this.schema, this.mappingAlias, this.selectedColumns);
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
        return !this.join.hasUniqueTarget();
    }

    @Override
    public boolean hasArrayDescendantNode() {
        return this.hasArrayDescendant;
    }

    public List<TableFilter> getJoinedFilters() {
        var list = new ArrayList<TableFilter>();
        for (EntityFilter q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                list.add(table);
            }
        }
        return list;
    }

    protected void gatherColumnMappings(List<QResultMapping> columnGroupMappings) {
        columnGroupMappings.add(this);
        this.hasArrayDescendant = false;
        for (EntityFilter q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                table.gatherColumnMappings(columnGroupMappings);
                this.hasArrayDescendant |= table.isArrayNode() || table.hasArrayDescendant;
            } else if (!q.isEmpty()) {
                getRootNode().disableJPQL();
            }
        }
    }

    protected void addSelection(HyperSelect.ResultMap resultMap) {
        QSchema schema = this.getSchema();
        boolean allLeaf = resultMap.isAllLeafSelected();
        if (allLeaf) {
            this.selectedColumns = schema.getBaseColumns();
        } else if ((this.isArrayNode() && this.hasJoinedChildMapping()) || resultMap.isIdSelected()) {
            this.selectedColumns = schema.getPKColumns();
        }

        for (Map.Entry<String, HyperSelect.ResultMap> entry : resultMap.entrySet()) {
            String key = entry.getKey();
            QColumn column = schema.findColumn(key);
            // TODO. Json Selection. is subMap required??
            HyperSelect.ResultMap subMap = entry.getValue();
            if (column != null && subMap.isEmpty()) {
                this.addSelectedColumn(column);
            } else {
                EntityFilter scope = this.makeSubNode(key, HqlParser.NodeType.Entity);
                scope.addSelection(subMap);
            }
        }
    }

    @Override
    public String toString() {
        return join != null ? join.getJsonKey() : schema.getTableName();
    }

    public void addOrderBy(Sort.Order order) {
        this.orders.add(order);
    }

    public List<Sort.Order> getOrders() {
        return orders;
    }

    public boolean hasAnySelectSubColumns() {
        if (this.selectedColumns != null && !this.selectedColumns.isEmpty()) return true;
        for (var f : subFilters.values()) {
            TableFilter tf = f.asTableFilter();
            if (tf != null && tf.hasAnySelectSubColumns()) return true;
        }
        return false;
    }
}
