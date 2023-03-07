package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlTable;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.util.KVEntity;

public class JdbcRepositoryImpl<ID> extends JdbcRepositoryBase<KVEntity, ID> {

    protected JdbcRepositoryImpl(JdbcStorage storage, QSchema schema) {
        super(storage, schema);
    }

    @Override
    public JqlTable<KVEntity, ID> getRawTable() {
        return this;
    }
}
