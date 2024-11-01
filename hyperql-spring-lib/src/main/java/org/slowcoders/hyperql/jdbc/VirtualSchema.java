package org.slowcoders.hyperql.jdbc;

import org.slowcoders.hyperql.jdbc.storage.JdbcSchema;

public class VirtualSchema extends JdbcSchema {

    private final String filter;
    private final String[] defaultFilterParams;

    protected VirtualSchema(JdbcStorage schemaLoader, String tableName, String filter, String[] defaultFilterParams) {
        super(schemaLoader, tableName, null);
        this.filter = filter;
        this.defaultFilterParams = defaultFilterParams;
    }

    @Override
    public String getSampleQuery() {
        SqlWriter sw = new SqlWriter();
        sw.writeF(filter, defaultFilterParams);
        return sw.toString();
    }

    @Override
    public String getTableExpression(String[] params) {
        if (params == null || params.length == 0) {
            params = defaultFilterParams;
        }
        SqlWriter sw = new SqlWriter();
        sw.write('(');
        sw.writeF(filter, params);
        sw.write(')');
        return sw.toString();
    }


}
