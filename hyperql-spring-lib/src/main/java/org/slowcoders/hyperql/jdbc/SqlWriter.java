package org.slowcoders.hyperql.jdbc;

import org.slowcoders.hyperql.util.SourceWriter;

public class SqlWriter extends SourceWriter<SqlWriter> {
    public SqlWriter() {
        super('\'', "''");
    }
}
