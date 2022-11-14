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
        return getRowFilter().getSchema();
    }

    @Override
    public Filter getQueryScope_impl(String key, Type type, boolean fetchData_unused) {
        if (type == Type.Leaf) {
            return this;
        }
        Filter entity = subQueries.get(key);
        if (entity == null) {
            entity = new JsonFilter(this, key);
            subQueries.put(key, entity);
//            super.add(entity);
        }
        return entity;
    }

    public JsonFilter asJsonFilter() {
        return this;
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

//    @Override
//    public QScope createQueryScope(Conjunction conjunction) {
//        return new JsonFilter(this.parent, this.key, conjunction);
//    }

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
