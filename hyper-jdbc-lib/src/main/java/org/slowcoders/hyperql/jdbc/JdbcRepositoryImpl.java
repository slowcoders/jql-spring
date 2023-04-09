package org.slowcoders.hyperql.jdbc;

import org.slowcoders.hyperql.schema.QSchema;

public class JdbcRepositoryImpl<ID> extends JdbcRepositoryBase<ID> {

    protected JdbcRepositoryImpl(JdbcStorage storage, QSchema schema) {
        super(storage, schema);
    }

}
