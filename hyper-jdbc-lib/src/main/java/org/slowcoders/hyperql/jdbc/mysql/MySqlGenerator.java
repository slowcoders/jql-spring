package org.slowcoders.hyperql.jdbc.mysql;

import org.slowcoders.hyperql.EntitySet;
import org.slowcoders.hyperql.jdbc.storage.JdbcSchema;
import org.slowcoders.hyperql.jdbc.storage.SqlGenerator;
import org.slowcoders.hyperql.js.JsType;
import org.slowcoders.hyperql.parser.EntityFilter;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;

import java.util.List;
import java.util.Map;

public class MySqlGenerator extends SqlGenerator {
    public MySqlGenerator(boolean isNativeQuery) {
        super(isNativeQuery);
    }

    private void writeInsertHeader(QSchema schema, EntitySet.InsertPolicy insertPolicy) {
        sw.writeln();
        sw.write("INSERT ");
        if (insertPolicy == EntitySet.InsertPolicy.IgnoreOnConflict) sw.write("IGNORE ");
        sw.write("INTO ").write(schema.getTableName());
    }

    public String createInsertStatement(JdbcSchema schema, Map<String, Object> entity, EntitySet.InsertPolicy insertPolicy) {
        this.writeInsertHeader(schema, insertPolicy);

        super.writeInsertStatementInternal(schema, entity);

        switch (insertPolicy) {
            case IgnoreOnConflict:
                sw.write("\nON CONFLICT DO NOTHING");
                break;
            case UpdateOnConflict:
                if (!schema.hasGeneratedId()) {
                    sw.write("\nON CONFLICT DO UPDATE");
                    for (Map.Entry<String, Object> entry : entity.entrySet()) {
                        String col = schema.getColumn(entry.getKey()).getPhysicalName();
                        sw.write("  ");
                        sw.write(col).write(" = VALUES(").write(col).write("),\n");
                    }
                    sw.replaceTrailingComma("\n");
                }
        }
        String sql = sw.reset();
        return sql;
    }


    public String prepareBatchInsertStatement(JdbcSchema schema, EntitySet.InsertPolicy insertPolicy) {
        this.writeInsertHeader(schema, insertPolicy);

        super.writePreparedInsertStatementValueSet((List)schema.getWritableColumns());

        switch (insertPolicy) {
            case UpdateOnConflict:
                if (!schema.hasGeneratedId()) {
                    sw.write("\nON DUPLICATE KEY UPDATE\n"); // SET 포함 안 함? 아래 문장 하나로 해결??
                    for (QColumn column : schema.getWritableColumns()) {
                        String col = column.getPhysicalName();
                        sw.write("  ");
                        sw.write(col).write(" = VALUES(").write(col).write("),\n");
                    }
                    sw.replaceTrailingComma("\n");
                }
        }
        String sql = sw.reset();
        return sql;
    }

    protected void writeJsonPath(EntityFilter node, QColumn column, JsType valueType) {
        writeJsonPath(node);
        sw.write(column.getJsonKey()).write('\'');
    }

    private void writeJsonPath(EntityFilter node) {
        if (node.isJsonNode()) {
            EntityFilter parent = node.getParentNode();
            writeJsonPath(parent);
            sw.write(node.getMappingAlias());
            if (!parent.isJsonNode()) {
                sw.write("->'$");
            }
        } else {
            sw.write(node.getMappingAlias());
        }
        sw.write('.');
    }
}
