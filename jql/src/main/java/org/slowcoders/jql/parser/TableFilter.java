package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;
import org.slowcoders.jql.jdbc.JqlResultMapping;

import java.util.List;

public class TableFilter extends Filter implements JqlResultMapping {
    private final JqlSchema schema;

    private final JqlEntityJoin join;
    private String[] entityMappingPath;
    private static final String[] emptyPath = new String[0];

    protected TableFilter(JqlSchema schema) {
        super(null);
        this.schema = schema;
        this.join = null;
        this.entityMappingPath = emptyPath;
    }

    public TableFilter(TableFilter baseFilter, JqlEntityJoin join) {
        super(baseFilter);
        this.schema = join.getAssociatedSchema();
        this.join = join;
    }

    public JqlSchema getSchema() {
        return schema;
    }

    public TableFilter asTableFilter() {
        return this;
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
    public List<JqlColumn> getSelectColumns() {
        return null;
    }

    public TableFilter getTableFilter() {
        return this;
    }

    @Override
    public Filter getFilter_impl(String key, ValueNodeType nodeType, boolean fetchData) {
        JqlEntityJoin join = schema.getEntityJoinBy(key);
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
        sb.write(getSchema().getTableName()).write(".").write(key);
    }

    @Override
    public void accept(JqlPredicateVisitor visitor) {
        JqlSchema old = visitor.setWorkingSchema(this.schema);
        super.accept(visitor);
        visitor.setWorkingSchema(old);
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

    public JqlEntityJoin getEntityJoin() {
        return this.join;
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
