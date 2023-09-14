package org.slowcoders.hyperql.sample.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.config.DefaultHyperStorageConfig;
import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;

@Configuration
public class HyperJdbcConfig extends DefaultHyperStorageConfig {

    @Bean
    public JdbcStorage jdbcStorage(DataSource dataSource, TransactionTemplate transactionTemplate,
                                  ObjectMapper objectMapper,
                                  EntityManager entityManager) throws Exception {
        JdbcStorage storage = new JdbcStorage(dataSource, transactionTemplate,
                objectMapper, entityManager);
        return storage;
    }
}
