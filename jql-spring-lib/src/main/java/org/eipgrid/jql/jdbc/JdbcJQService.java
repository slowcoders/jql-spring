package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.JQSchemaLoader;
import org.eipgrid.jql.jdbc.metadata.JdbcSchemaLoader;
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

public class JdbcJQService extends JQService {
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
}
