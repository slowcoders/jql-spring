package org.slowcoders.hyperql.jdbc.storage;

import org.slowcoders.hyperql.jdbc.JdbcStorage;

import java.sql.Connection;
import java.sql.SQLException;

public interface SchemaLoaderFactory {
    JdbcSchemaLoader createSchemaLoader(JdbcStorage storage, Connection conn) throws SQLException;
}
