package org.eipgrid.jql.jdbc.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.JqlRepository;
import org.eipgrid.jql.JqlStorage;
import org.eipgrid.jql.jdbc.JdbcStorage;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.util.ClassUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public abstract class JdbcSchemaLoader extends JqlStorage {
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbc;
    private HashMap<String, Class<?>> ormTypeMap;

    private final HashMap<Class<?>, JdbcSchema> classToSchemaMap = new HashMap<>();
    private final HashMap<String, JdbcSchema> schemaMap = new HashMap<>();

    private final MetadataLoader metadataLoader;

    private String dbType;

    protected JdbcSchemaLoader(DataSource dataSource, TransactionTemplate transactionTemplate, ObjectMapper objectMapper, EntityManager entityManager) {
        super(transactionTemplate, objectMapper);
        this.jdbc = new JdbcTemplate(dataSource);
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
        this.metadataLoader = jdbc.execute(new ConnectionCallback<MetadataLoader>() {
            @Override
            public MetadataLoader doInConnection(Connection conn) throws SQLException, DataAccessException {
                dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
                String factoryName = JdbcStorage.class.getPackageName() + '.' + dbType + '.' + "SchemaLoaderFactory";
                SchemaLoaderFactory factory = ClassUtils.newInstanceOrNull(factoryName);
                return factory.createSchemaLoader(JdbcSchemaLoader.this, conn);
            }
        });
    }

    public interface SchemaLoaderFactory {
        MetadataLoader createSchemaLoader(JdbcSchemaLoader storage, Connection conn) throws SQLException;
    }

    public String getDbType() {
        return this.dbType;
    }


    public final EntityManager getEntityManager() { return entityManager; }

    public final DataSource getDataSource() {
        return this.jdbc.getDataSource();
    }

    public final JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }

    private void initialize() {
        if (ormTypeMap != null) return;
        synchronized (this) {
            if (ormTypeMap != null) return;
            ormTypeMap = new HashMap<>();

            Set<EntityType<?>> types = entityManager.getEntityManagerFactory().getMetamodel().getEntities();
            for (EntityType<?> type : types) {
                Class<?> clazz = type.getJavaType();
                MetadataLoader.TablePath tablePath = metadataLoader.getTablePath(clazz);
                if (tablePath != null) {
                    ormTypeMap.put(tablePath.getQualifiedName(), clazz);
                }
            }
        }
    }


    public QSchema loadSchema(Class entityType) {
        initialize();
        QSchema schema = classToSchemaMap.get(entityType);
        if (schema == null) {
            MetadataLoader.TablePath tablePath = metadataLoader.getTablePath(entityType);
            schema = loadSchema(tablePath, entityType);
        }
        return schema;
    }

    public JdbcSchema loadSchema(String tableName) {
        initialize();
        JdbcSchema schema = schemaMap.get(tableName);
        if (schema == null) {
            Class<?> ormType = ormTypeMap.get(tableName);
            if (ormType == null) {
                ormType = JqlRepository.rawEntityType;
            }
            MetadataLoader.TablePath tablePath = MetadataLoader.TablePath.of(tableName);
            schema = loadSchema(tablePath, ormType);
        }
        return schema;
    }

    private JdbcSchema loadSchema(MetadataLoader.TablePath tablePath, Class<?> ormType0) {

        final Class<?> ormType = ormType0;
        synchronized (schemaMap) {
            JdbcSchema schema = jdbc.execute(new ConnectionCallback<JdbcSchema>() {
                @Override
                public JdbcSchema doInConnection(Connection conn) throws SQLException, DataAccessException {
                    JdbcSchema schema = schemaMap.get(tablePath.getQualifiedName());
                    if (schema == null) {
                        schema = metadataLoader.loadSchema(conn, tablePath, ormType);
                        schemaMap.put(tablePath.getQualifiedName(), schema);
                        if (schema.isJPARequired()) {
                            classToSchemaMap.put(ormType, schema);
                        }
                    }
                    return schema;
                }
            });
            return schema;
        }
    }


    protected void loadJoinMap(QSchema schema) {
        synchronized (schemaMap) {
            jdbc.execute(new ConnectionCallback<Void>() {
                @Override
                public Void doInConnection(Connection conn) throws SQLException, DataAccessException {
                    metadataLoader.loadExternalJoins(conn, (JdbcSchema) schema);
                    return null;
                }
            });
        }
    }

    public List<String> getTableNames(String namespace) {
        List<String> tableNames = jdbc.execute(new ConnectionCallback<List<String>>() {
            @Override
            public List<String> doInConnection(Connection conn) throws SQLException, DataAccessException {
                return metadataLoader.getTableNames(conn, namespace);
            }
        });
        return tableNames;
    }

    @Override
    public List<String> getNamespaces() {
        List<String> namespaces = jdbc.execute(new ConnectionCallback<List<String>>() {
            @Override
            public List<String> doInConnection(Connection conn) throws SQLException, DataAccessException {
                return metadataLoader.getNamespaces(conn);
            }
        });
        return namespaces;
    }


    protected SqlGenerator createSqlGenerator(boolean isNativeQuery) {
        return metadataLoader.createSqlGenerator(isNativeQuery);
    }

    public String getTableComment(String tableName) {
        return metadataLoader.getTableComment(tableName);
    }
}
