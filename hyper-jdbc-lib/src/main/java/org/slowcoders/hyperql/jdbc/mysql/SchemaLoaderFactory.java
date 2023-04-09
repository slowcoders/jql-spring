package org.slowcoders.hyperql.jdbc.mysql;

import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.slowcoders.hyperql.jdbc.storage.JdbcSchemaLoader;

import java.sql.Connection;
import java.sql.SQLException;

public class SchemaLoaderFactory implements org.slowcoders.hyperql.jdbc.storage.SchemaLoaderFactory {
    @Override
    public JdbcSchemaLoader createSchemaLoader(JdbcStorage storage, Connection conn) throws SQLException {
        return new MySqlSchemaLoader(storage, conn);
    }
}
