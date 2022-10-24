package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class JsonNode extends QNode {
    private final QNode parent;
    private final String key;

    JsonNode(QNode parentNode, String key) {
        this(parentNode, key, Conjunction.AND);
    }

    JsonNode(QNode parentNode, String key, Conjunction conjunction) {
        super(conjunction);
        this.parent = parentNode;
        this.key = key;
    }

    public JqlSchema getSchema() {
        return getTable().getSchema();
    }

    public TableNode getTable() {
        return parent.getTable();
    }

    @Override
    public QNode getContainingFilter_impl(JqlQuery query, String key, boolean isLeaf, boolean fetchData_unused) {
        if (isLeaf) {
            return this;
        }
        QNode entity = subEntities.get(key);
        if (entity == null) {
            entity = new JsonNode(this, key);
            subEntities.put(key, entity);
            super.add(entity);
        }
        return entity;
    }

    public JsonNode asJsonFilter() {
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
    public QNode createFilter(Conjunction conjunction) {
        return new JsonNode(this.parent, this.key, conjunction);
    }

    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private void dumpColumnName(QueryBuilder sb) {
        JsonNode p = parent.asJsonFilter();
        if (p != null) {
            p.dumpColumnName(sb);
            sb.write(" -> '").write(key).write('\'');
        } else {
            sb.write(key);
        }
    }
}
