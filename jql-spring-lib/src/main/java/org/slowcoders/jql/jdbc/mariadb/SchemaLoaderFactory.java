package org.slowcoders.jql.jdbc.mariadb;

import org.slowcoders.jql.jdbc.mysql.MySqlSchemaLoader;
import org.slowcoders.jql.jdbc.JdbcStorage;
import org.slowcoders.jql.jdbc.storage.JdbcSchemaLoader;

import java.sql.Connection;
import java.sql.SQLException;

public class SchemaLoaderFactory implements org.slowcoders.jql.jdbc.storage.SchemaLoaderFactory {
    @Override
    public JdbcSchemaLoader createSchemaLoader(JdbcStorage storage, Connection conn) throws SQLException {
        return new MySqlSchemaLoader(storage, conn);
    }
}
