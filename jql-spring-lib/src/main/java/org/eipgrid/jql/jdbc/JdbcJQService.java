package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.JQSchemaLoader;
import org.eipgrid.jql.JQSelect;
import org.eipgrid.jql.jdbc.metadata.JdbcSchemaLoader;
import org.eipgrid.jql.parser.JqlQuery;
import org.eipgrid.jql.spring.JQRepository;
import org.eipgrid.jql.spring.JQService;
import org.eipgrid.jql.util.AttributeNameConverter;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcJQService extends JQService implements QueryBuilder {
    JdbcSchemaLoader jdbcSchemaLoader;
    private HashMap<String, JQRepository> repositories = new HashMap<>();

    public JdbcJQService(DataSource dataSource, TransactionTemplate transactionTemplate,
                         MappingJackson2HttpMessageConverter jsonConverter,
                         ConversionService conversionService,
                         RequestMappingHandlerMapping handlerMapping,
                         EntityManager entityManager,
                         EntityManagerFactory entityManagerFactory) throws Exception {
        super(dataSource, transactionTemplate, jsonConverter, conversionService,
                handlerMapping, entityManager, entityManagerFactory);
        jdbcSchemaLoader = new JdbcSchemaLoader(dataSource, AttributeNameConverter.defaultConverter);
    }

    public JQSchemaLoader getSchemaLoader() {
        return jdbcSchemaLoader;
    }

    public JQRepository makeRepository(String tableName) {
        JQRepository repo = repositories.get(tableName);
        if (repo == null) {
            JQSchema schema = jdbcSchemaLoader.loadSchema(tableName, null);
            repo = new JDBCRepositoryBase(this, schema);
            repositories.put(tableName, repo);
        }
        return repo;
    }

    public JQSchema loadSchema(String tablePath, Class entityType) {
        return jdbcSchemaLoader.loadSchema(tablePath, entityType);
    }

    public JQSchema loadSchema(Class entityType) {
        return jdbcSchemaLoader.loadSchema(entityType);
    }

    public List<String> getTableNames(String dbSchema) throws SQLException {
        return jdbcSchemaLoader.getTableNames(dbSchema);
    }

    public List<String> getDBSchemas() {
        return jdbcSchemaLoader.getDBSchemas();
    }

    @Override
    public String createSelectQuery(JqlQuery where, JQSelect columns) {
        return new SqlGenerator().createSelectQuery(where, columns);
    }

    @Override
    public String createCountQuery(JqlQuery where) {
        return new SqlGenerator().createCountQuery(where);
    }

    @Override
    public String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet) {
        return new SqlGenerator().createUpdateQuery(where, updateSet);
    }

    @Override
    public String createDeleteQuery(JqlQuery where) {
        return new SqlGenerator().createDeleteQuery(where);
    }

    @Override
    public String prepareFindByIdStatement(JQSchema schema) {
        return new SqlGenerator().prepareFindByIdStatement(schema);
    }

    @Override
    public String createInsertStatement(JQSchema schema, Map entity, boolean ignoreConflict) {
        return new SqlGenerator().createInsertStatement(schema, entity, ignoreConflict);
    }
}
