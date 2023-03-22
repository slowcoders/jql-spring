package org.eipgrid.jql.jdbc.postgresql;

import org.eipgrid.jql.jdbc.storage.JdbcSchemaLoader;
import org.eipgrid.jql.jdbc.storage.MetadataLoader;

import java.sql.Connection;
import java.sql.SQLException;

public class SchemaLoaderFactory implements JdbcSchemaLoader.SchemaLoaderFactory {
    @Override
    public MetadataLoader createSchemaLoader(JdbcSchemaLoader storage, Connection conn) throws SQLException {
        return new PGSchemaLoader(storage, conn);
    }
}
