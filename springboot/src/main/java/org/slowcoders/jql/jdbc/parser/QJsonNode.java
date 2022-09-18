package org.slowcoders.jql.jdbc.parser;

import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.ValueFormat;

class QJsonNode extends QNode {
    private final QNode parent;
    private final String key;

    QJsonNode(QNode parentNode, String key) {
        this(parentNode, key, Type.AND);
    }

    QJsonNode(QNode parentNode, String key, Type delimiter) {
        super(delimiter);
        this.parent = parentNode;
        this.key = key;
    }

    public JqlSchema getSchema() {
        return getTable().getSchema();
    }

    public QTableNode getTable() {
        return parent.getTable();
    }

    @Override
    public QNode getContainingEntity_impl(JqlQuery query, String key, boolean isLeaf) {
        if (isLeaf) {
            return this;
        }
        QNode entity = subEntities.get(key);
        if (entity == null) {
            entity = new QJsonNode(this, key);
            subEntities.put(key, entity);
            super.add(entity);
        }
        return entity;
    }

    public QJsonNode asJsonNode() {
        return this;
    }

    @Override
    public void writeAttribute(SQLWriter sb, String key, Class<?> valueType) {
        sb.write("(");
        this.dumpColumnName(sb);
        sb.write(" ->> '").write(key).write("')");
        ValueFormat vf = ValueFormat.resolveValueFormat(valueType);
        switch (vf) {
            case Int:
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
            case Collection:
            case Embedded:
                sb.write("::JSONB");
                break;
        }
    }

    @Override
    public QNode createQuerySet(Type type) {
        return new QJsonNode(this.parent, this.key, type);
    }

    @Override
    public String getColumnName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private void dumpColumnName(SQLWriter sb) {
        QJsonNode p = parent.asJsonNode();
        if (p != null) {
            p.dumpColumnName(sb);
            sb.write(" -> '").write(key).write('\'');
        } else {
            sb.write(key);
        }
    }
}
