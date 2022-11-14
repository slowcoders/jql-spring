package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class JsonFilter extends Filter {
    private final String key;

    JsonFilter(Filter parentQuery, String key) {
        super(parentQuery);
        this.key = key;
    }

    public JqlSchema getSchema() {
        return getTableFilter().getSchema();
    }

    public JsonFilter asJsonFilter() {
        return this;
    }

    @Override
    public Filter getFilter_impl(String key, ValueNodeType nodeType, boolean fetchData_unused) {
        if (nodeType == ValueNodeType.Leaf) {
            return this;
        }
        Filter entity = subFilters.get(key);
        if (entity == null) {
            entity = new JsonFilter(this, key);
            subFilters.put(key, entity);
        }
        return entity;
    }

    @Override
    public void writeAttribute(SourceWriter sb, String key, Class<?> valueType) {
        sb.write("(");
        this.dumpColumnName(sb);
        sb.write(" ->> '").write(key).write("')");
        JsonNodeType vf = JsonNodeType.getNodeType(valueType);
        switch (vf) {
            case Integer:
            case Float:
                sb.write("::NUMERIC");
                break;
            case Date:
                sb.write("::DATE");
                break;
            case Time:
                sb.write("::TIME");
                break;
            case Timestamp:
                sb.write("::TIMESTAMP");
                break;
            case Text:
                sb.write("::TEXT");
                break;
            case Array:
            case Object:
                sb.write("::JSONB");
                break;
        }
    }

    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private void dumpColumnName(SourceWriter sb) {
        JsonFilter p = getParent().asJsonFilter();
        if (p != null) {
            p.dumpColumnName(sb);
            sb.write(" -> '").write(key).write('\'');
        } else {
            sb.write(key);
        }
    }

}
