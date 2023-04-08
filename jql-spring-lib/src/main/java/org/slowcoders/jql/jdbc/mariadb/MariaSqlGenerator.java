package org.slowcoders.jql.jdbc.mariadb;

import org.slowcoders.jql.jdbc.mysql.MySqlGenerator;
import org.slowcoders.jql.js.JsType;
import org.slowcoders.jql.parser.EntityFilter;
import org.slowcoders.jql.schema.QColumn;

public class MariaSqlGenerator extends MySqlGenerator {
    public MariaSqlGenerator(boolean isNativeQuery) {
        super(isNativeQuery);
    }

    protected void writeJsonPath(EntityFilter node, QColumn column, JsType valueType) {
        sw.write("json_value(");
        writeJsonPath(node);
        sw.write(column.getJsonKey()).write('\'');
        sw.write(')');
    }

    private void writeJsonPath(EntityFilter node) {
        if (node.isJsonNode()) {
            EntityFilter parent = node.getParentNode();
            writeJsonPath(parent);
            if (!parent.isJsonNode()) {
                sw.write("`").write(node.getMappingAlias()).write("`, '$");
            } else {
                sw.write(node.getMappingAlias());
            }
        } else {
            sw.write(node.getMappingAlias());
        }
        sw.write('.');
    }
}
