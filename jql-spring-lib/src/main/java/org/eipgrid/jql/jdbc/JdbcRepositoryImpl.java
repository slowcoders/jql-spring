package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.schema.QSchema;

public class JdbcRepositoryImpl<ID> extends JdbcRepositoryBase<ID> {

    protected JdbcRepositoryImpl(JdbcStorage storage, QSchema schema) {
        super(storage, schema);
    }

}
