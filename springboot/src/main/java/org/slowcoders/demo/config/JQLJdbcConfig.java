package org.slowcoders.demo.config;

import org.slowcoders.jql.jdbc.JQLJdbcService;
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
public class JQLJdbcConfig {

    @Bean
    public JQLJdbcService jqlJdbcService(DataSource dataSource, TransactionTemplate transactionTemplate,
                                         MappingJackson2HttpMessageConverter jsonConverter,
                                         ConversionService conversionService,
                                         RequestMappingHandlerMapping handlerMapping,
                                         EntityManager entityManager,
                                         EntityManagerFactory entityManagerFactory) throws Exception {
        JQLJdbcService service = new JQLJdbcService(dataSource, transactionTemplate, jsonConverter,
                conversionService, handlerMapping, entityManager, entityManagerFactory);
        return service;
    }
}
