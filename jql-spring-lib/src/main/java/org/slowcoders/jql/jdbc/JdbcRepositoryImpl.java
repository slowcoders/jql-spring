package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.schema.QSchema;

public class JdbcRepositoryImpl<ID> extends JdbcRepositoryBase<ID> {

    protected JdbcRepositoryImpl(JdbcStorage storage, QSchema schema) {
        super(storage, schema);
    }

}
