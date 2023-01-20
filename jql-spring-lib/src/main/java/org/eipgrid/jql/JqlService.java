package org.eipgrid.jql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.jdbc.postgres.UpdateListener;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.eipgrid.jql.util.AttributeNameConverter;
import org.eipgrid.jql.util.ClassUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.util.HashMap;

@Service
public abstract class JqlService implements AttributeNameConverter {
    private final JdbcTemplate jdbc;
    private final MappingJackson2HttpMessageConverter jsonConverter;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final PhysicalNamingStrategy namingStrategy;
    private final ConversionService conversionService;

    private final RequestMappingHandlerMapping handlerMapping;
    private HashMap<String, JqlRepository> repositories = new HashMap<>();

    public JqlService(DataSource dataSource,
                      TransactionTemplate transactionTemplate,
                      MappingJackson2HttpMessageConverter jsonConverter,
                      ConversionService conversionService,
                      RequestMappingHandlerMapping handlerMapping,
                      EntityManager entityManager, EntityManagerFactory entityManagerFactory) throws Exception {
        this.jdbc = new JdbcTemplate(dataSource);
        this.objectMapper = new ObjectMapper();
        this.transactionTemplate = transactionTemplate;
        this.jsonConverter = jsonConverter;
        this.conversionService = conversionService;
        this.handlerMapping = handlerMapping;
        this.entityManager = entityManager;
        String cname = (String) entityManagerFactory.getProperties().get("hibernate.physical_naming_strategy");
        this.namingStrategy = ClassUtils.newInstanceOrNull(cname);
        System.out.println(cname);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }

    public EntityManager getEntityManager() { return entityManager; }

    public MappingJackson2HttpMessageConverter getJsonConverter() {
        return jsonConverter;
    }

    public ConversionService getConversionService() {
        return this.conversionService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public abstract JqlRepository makeRepository(String tableName);

    public abstract QSchema loadSchema(String tableName, Class ormType);

    public abstract QSchema loadSchema(Class ormType);

    public String resolveTableName(Class<?> entityType) {
        String name = "";
        Table table = entityType.getAnnotation(Table.class);
        String schema = "";
        if (table != null) {
            name = table.name().trim();
            schema = table.schema().trim();
        }
        if (name.length() == 0) {
            name = entityType.getSimpleName();
        }
        return makeTablePath(schema, name);
    }

    public String makeTablePath(String schema, String name) {
        name = schema + "." + name;
        return name;
    }

    public String toPhysicalColumnName(String fieldName) {
        Identifier physicalName = this.namingStrategy.toPhysicalColumnName(Identifier.toIdentifier(fieldName, false), null);
        return physicalName.getCanonicalName();
    }

    @Override
    public String toLogicalAttributeName(String columnName) {
        throw new RuntimeException("not implemented");
    }

    public <ID, ENTITY> void registerRepository(JPARepositoryBase<ENTITY,ID> repository) {
        String qname = resolveTableName(repository.getEntityType());
        UpdateListener.initAutoUpdateTrigger(this, qname, repository);

        Object old = this.repositories.put(qname, repository);
        assert (old == null);
    }


    public DataSource getDataSource() {
        return this.jdbc.getDataSource();
    }

    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }
}
