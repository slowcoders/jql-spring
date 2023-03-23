package org.eipgrid.jql.jdbc.postgresql;

import org.eipgrid.jql.JqlEntitySet;
import org.eipgrid.jql.jdbc.storage.JdbcColumn;
import org.eipgrid.jql.jdbc.storage.JdbcSchema;
import org.eipgrid.jql.jdbc.storage.SqlConverter;
import org.eipgrid.jql.jdbc.storage.SqlGenerator;
import org.eipgrid.jql.schema.QColumn;

import java.util.List;
import java.util.Map;

public class PGSqlGenerator extends SqlGenerator {
    public PGSqlGenerator(boolean isNativeQuery) {
        super(isNativeQuery);
    }

    public String createInsertStatement(JdbcSchema schema, Map entity, JqlEntitySet.InsertPolicy insertPolicy) {
        sw.writeln();
        sw.write(getCommand(SqlConverter.Command.Insert)).write(" INTO ").write(schema.getTableName());

        super.writeInsertStatementInternal(schema, entity);

        switch (insertPolicy) {
            case IgnoreOnConflict:
                sw.write("\nON CONFLICT DO NOTHING");
                break;
            case UpdateOnConflict:
                if (!schema.hasGeneratedId()) {
                    sw.write("\nON CONFLICT DO UPDATE SET");
                    super.writeUpdateValueSet(schema, entity);
                }
        }
        String sql = sw.reset();
        return sql;
    }


    protected void writePreparedInsertStatementValueSet(List<JdbcColumn> columns) {
        sw.writeln("(");
        for (QColumn col : columns) {
            sw.write(col.getPhysicalName()).write(", ");
        }
        sw.replaceTrailingComma("\n) VALUES (");
        for (JdbcColumn column : columns) {
            String dbType = column.getDBColumnType();
            sw.write("?::").write(dbType).write(", ");
        }
        sw.replaceTrailingComma(")");
    }

    @Override
    public String prepareBatchInsertStatement(JdbcSchema schema, JqlEntitySet.InsertPolicy insertPolicy) {
        sw.writeln();
        sw.write("INSERT INTO ").write(schema.getTableName());

        this.writePreparedInsertStatementValueSet((List)schema.getWritableColumns());

        switch (insertPolicy) {
            case IgnoreOnConflict:
                sw.write("\nON CONFLICT DO NOTHING");
                break;
            case UpdateOnConflict:
                if (!schema.hasGeneratedId()) {
                    sw.write("\nON CONFLICT(");
                    for (QColumn col : schema.getPKColumns()) {
                        sw.write(col.getPhysicalName()).write(", ");
                    }
                    sw.replaceTrailingComma(") DO UPDATE SET\n");
                    for (QColumn col : schema.getWritableColumns()) {
                        if (!col.isPrimaryKey()) {
                            String col_name = col.getPhysicalName();
                            sw.write(col_name).write(" = excluded.").write(col_name).write(",\n");
                        }
                    }
                    sw.replaceTrailingComma(";");
                }
        }
        String sql = sw.reset();
        return sql;
    }

}
