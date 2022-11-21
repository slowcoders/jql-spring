package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;
import org.slowcoders.jql.jdbc.JqlResultMapping;

import java.util.List;

public class TableFilter extends Filter implements JqlResultMapping {
    private final JqlSchema schema;

    private final JqlSchemaJoin join;
    private final String mappingAlias;
    private String[] entityMappingPath;
    private List<JqlColumn> selectedColumns;
    private static final String[] emptyPath = new String[0];
    private List<JqlResultMapping> subMappingNodes;

    protected TableFilter(JqlSchema schema, String mappingAlias) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
        this.mappingAlias = mappingAlias;
    }

    public TableFilter(TableFilter baseFilter, JqlSchemaJoin join) {
        super(baseFilter);
        this.schema = join.getAssociatedSchema();
        this.join = join;
        this.mappingAlias = baseFilter.getRootFilter().createUniqueMappingAlias();
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public TableFilter asTableFilter() {
        return this;
    }

    public JqlResultMapping getContainingMapping() { return getParent().asTableFilter(); }

    @Override
    public String getMappingAlias() {
        return mappingAlias;
    }

    public String[] getEntityMappingPath() {
        String[] jsonPath = this.entityMappingPath;
        if (jsonPath == null) {
            String[] basePath = getParent().asTableFilter().getEntityMappingPath();
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
        return selectedColumns != null ? selectedColumns : schema.getReadableColumns();
    }

    public TableFilter getTableFilter() {
        return this;
    }

    @Override
    public Filter getFilter_impl(String key, ValueNodeType nodeType, boolean fetchData) {
        JqlSchemaJoin join = schema.getEntityJoinBy(key);
        if (join == null) {
            JqlColumn column = schema.getColumn(key);
            if (column.getValueFormat() != JsonNodeType.Object) return this;
        }

        Filter subQuery = subFilters.get(key);
        if (subQuery == null) {
            if (join != null) {
//                JqlSchema subSchema = getTopQuery().addTableJoin(join, fetchData);
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
    public void writeAttribute(SourceWriter sb, String key, Class<?> valueType) {
        sb.write(mappingAlias).write(".").write(key);
    }

    @Override
    public void accept(JqlPredicateVisitor visitor) {
        JqlSchema old = visitor.setWorkingSchema(this.schema, this.mappingAlias);
        super.accept(visitor);
        visitor.setWorkingSchema(old, this.mappingAlias);
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

    public JqlSchemaJoin getEntityJoin() {
        return this.join;
    }

    @Override
    public boolean isArrayNode() {
        return !this.join.isUniqueJoin();
    }

    protected void gatherColumnMappings(List<JqlResultMapping> columnGroupMappings) {
        columnGroupMappings.add(this);
        for (Filter q : subFilters.values()) {
            TableFilter table = q.asTableFilter();
            if (table != null) {
                table.gatherColumnMappings(columnGroupMappings);
            }
        }
    }
}
