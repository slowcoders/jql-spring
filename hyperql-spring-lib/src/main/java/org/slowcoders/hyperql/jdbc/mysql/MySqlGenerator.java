package org.slowcoders.hyperql.jdbc.mysql;

import org.slowcoders.hyperql.EntitySet;
import org.slowcoders.hyperql.jdbc.storage.JdbcColumn;
import org.slowcoders.hyperql.jdbc.storage.JdbcSchema;
import org.slowcoders.hyperql.jdbc.storage.SqlGenerator;
import org.slowcoders.hyperql.js.JsType;
import org.slowcoders.hyperql.parser.EntityFilter;
import org.slowcoders.hyperql.parser.HqlOp;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;

import java.util.Collection;
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

    protected String toSqlExpression(HqlOp operator) {
        return switch (operator) {
            case RE, RE_ignoreCase -> " REGEXP ";
            case NOT_RE, NOT_RE_ignoreCase -> " NOT REGEXP ";
            default -> super.toSqlExpression(operator);
        };
    }

    @Override
    public void visitCompareArray(QColumn column, HqlOp operator, Collection values) {
        boolean isNot = false;
        switch (operator) {
            case NOT_CONTAINS:
                sw.write("NOT ");
            case CONTAINS:
                sw.write("(");
                for (var v : values) {
                    sw.writeValue(v);
                    sw.write(" MEMBER OF(");
                    writeQualifiedColumnName(column, values);
                    sw.write(") AND ");
                }
                sw.shrinkLength(5);
                sw.write(")");
                break;

            case NOT_OVERLAPS:
                sw.write("NOT ");
            case OVERLAPS:
                sw.write("JSON_OVERLAPS(");
                writeQualifiedColumnName(column, values);
                sw.write(", JSON_ARRAY(");
                sw.writeValues(values);
                sw.write(")");
                sw.write(")");
                break;
        }
    }

    @Override
    protected String getJsonArrayAggregateFunction() {
        return "JSON_ARRAYAGG";
    }

    @Override
    protected String getJsonBuildArrayFunction() {
        return "JSON_ARRAY";
    }

    @Override
    public void visitPredicate(QColumn column, HqlOp operator, Object value) {
        switch (operator) {
            case NOT_RE: case NOT_RE_ignoreCase:
                sw.write(" NOT");
            case RE: case RE_ignoreCase:
                assert (value != null);
                sw.write(" REGEXP_LIKE(");
                boolean ic = (operator == HqlOp.NOT_RE_ignoreCase || operator == HqlOp.RE_ignoreCase);
                this.writeQualifiedColumnName(column, value);
                sw.write(", ").writeQuoted(value).write(ic ? ", 'i')" : ", 'c')");
                break;
            default:
                super.visitPredicate(column, operator, value);
        }
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


    public String prepareBatchInsertStatement(JdbcSchema schema, List<JdbcColumn> columns, EntitySet.InsertPolicy insertPolicy) {
        this.writeInsertHeader(schema, insertPolicy);

        super.writePreparedInsertStatementValueSet(columns);

        switch (insertPolicy) {
            case UpdateOnConflict:
                if (!schema.hasGeneratedId()) {
                    sw.write("\nON DUPLICATE KEY UPDATE\n"); // SET 포함 안 함? 아래 문장 하나로 해결??
                    for (QColumn column : columns) {
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

    protected void writeQualifiedJsonPath(EntityFilter node, QColumn column, JsType valueType) {
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
