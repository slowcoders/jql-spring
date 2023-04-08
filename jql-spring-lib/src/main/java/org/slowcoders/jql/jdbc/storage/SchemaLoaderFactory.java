package org.slowcoders.jql.jdbc.storage;

import org.slowcoders.jql.jdbc.JdbcStorage;

import java.sql.Connection;
import java.sql.SQLException;

public interface SchemaLoaderFactory {
    JdbcSchemaLoader createSchemaLoader(JdbcStorage storage, Connection conn) throws SQLException;
}
