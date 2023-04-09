package org.slowcoders.hyperql.jdbc.mariadb;

import org.slowcoders.hyperql.jdbc.mysql.MySqlGenerator;
import org.slowcoders.hyperql.js.JsType;
import org.slowcoders.hyperql.parser.EntityFilter;
import org.slowcoders.hyperql.schema.QColumn;

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
