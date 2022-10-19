package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JQLRepository;
import org.slowcoders.jql.JQLService;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.SchemaLoader;
import org.slowcoders.jql.jdbc.metadata.MetadataProcessor;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Service
public class JQLJdbcService extends JQLService {
    MetadataProcessor metadataProcessor;
    private HashMap<String, JQLRepository> repositories = new HashMap<>();

    public JQLJdbcService(DataSource dataSource, TransactionTemplate transactionTemplate,
                          MappingJackson2HttpMessageConverter jsonConverter,
                          ConversionService conversionService,
                          RequestMappingHandlerMapping handlerMapping,
                          EntityManager entityManager,
                          EntityManagerFactory entityManagerFactory) throws Exception {
        super(dataSource, transactionTemplate, jsonConverter, conversionService,
                handlerMapping, entityManager, entityManagerFactory);
        metadataProcessor = new MetadataProcessor(dataSource, AttributeNameConverter.defaultConverter);
    }

    public SchemaLoader getSchemaLoader() {
        return metadataProcessor;
    }

    public JQLRepository makeRepository(String tableName) {
        JQLRepository repo = repositories.get(tableName);
        if (repo == null) {
            JqlSchema jqlSchema = metadataProcessor.loadSchema(tableName);
            repo = new JDBCRepositoryBase(this, jqlSchema);
            repositories.put(tableName, repo);
        }
        return repo;
    }

    public JqlSchema loadSchema(String tablePath) {
        return metadataProcessor.loadSchema(tablePath);
    }

    public JqlSchema loadSchema(Class entityType) {
        return metadataProcessor.loadSchema(entityType);
    }

    public List<String> getTableNames(String dbSchema) throws SQLException {
        return metadataProcessor.getTableNames(dbSchema);
    }

    public List<String> getDBSchemas() {
        return metadataProcessor.getDBSchemas();
    }
}
