package org.eipgrid.jql.sample.config;

import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.eipgrid.jql.config.DefaultJqlConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class JdbcJqlConfig extends DefaultJqlConfig {

    @Bean
    public JdbcJqlService jdbcJqlService(DataSource dataSource, TransactionTemplate transactionTemplate,
                                         MappingJackson2HttpMessageConverter jsonConverter,
                                         ConversionService conversionService,
                                         RequestMappingHandlerMapping handlerMapping,
                                         EntityManager entityManager,
                                         EntityManagerFactory entityManagerFactory) throws Exception {
        JdbcJqlService service = new JdbcJqlService(dataSource, transactionTemplate, jsonConverter,
                conversionService, handlerMapping, entityManager, entityManagerFactory);
        return service;
    }
}
