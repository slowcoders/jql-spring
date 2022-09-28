package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JsonNodeType;

class JsonNode extends EntityNode {
    private final EntityNode parent;
    private final String key;

    JsonNode(EntityNode parentNode, String key) {
        this(parentNode, key, Conjunction.AND);
    }

    JsonNode(EntityNode parentNode, String key, Conjunction delimiter) {
        super(delimiter);
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
    public EntityNode getContainingEntity_impl(JqlQuery query, String key, boolean isLeaf) {
        if (isLeaf) {
            return this;
        }
        EntityNode entity = subEntities.get(key);
        if (entity == null) {
            entity = new JsonNode(this, key);
            subEntities.put(key, entity);
            super.add(entity);
        }
        return entity;
    }

    public JsonNode asJsonNode() {
        return this;
    }

    @Override
    public void writeAttribute(SQLWriter sb, String key, Class<?> valueType) {
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
    public EntityNode createQuerySet(Conjunction conjunction) {
        return new JsonNode(this.parent, this.key, conjunction);
    }

    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private void dumpColumnName(SQLWriter sb) {
        JsonNode p = parent.asJsonNode();
        if (p != null) {
            p.dumpColumnName(sb);
            sb.write(" -> '").write(key).write('\'');
        } else {
            sb.write(key);
        }
    }
}
