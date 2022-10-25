package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class JsonScope extends QScope {
    private final QScope parent;
    private final String key;

    JsonScope(QScope parentNode, String key) {
        this(parentNode, key, Conjunction.AND);
    }

    JsonScope(QScope parentNode, String key, Conjunction conjunction) {
        super(conjunction);
        this.parent = parentNode;
        this.key = key;
    }

    public JqlSchema getSchema() {
        return getTable().getSchema();
    }

    public TableScope getTable() {
        return parent.getTable();
    }

    @Override
    public QScope getQueryScope_impl(JqlQuery query, String key, boolean isLeaf, boolean fetchData_unused) {
        if (isLeaf) {
            return this;
        }
        QScope entity = subEntities.get(key);
        if (entity == null) {
            entity = new JsonScope(this, key);
            subEntities.put(key, entity);
            super.add(entity);
        }
        return entity;
    }

    public JsonScope asJsonScope() {
        return this;
    }

    @Override
    public void writeAttribute(QueryBuilder sb, String key, Class<?> valueType) {
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
    public QScope createQueryScope(Conjunction conjunction) {
        return new JsonScope(this.parent, this.key, conjunction);
    }

    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private void dumpColumnName(QueryBuilder sb) {
        JsonScope p = parent.asJsonScope();
        if (p != null) {
            p.dumpColumnName(sb);
            sb.write(" -> '").write(key).write('\'');
        } else {
            sb.write(key);
        }
    }
}
