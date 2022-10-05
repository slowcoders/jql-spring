package org.slowcoders.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.time.ZoneId;
import java.util.Properties;

@Configuration
public class DBConfig {

    @Bean("db-config")
    @ConfigurationProperties(prefix = "spring.datasource")
    public Properties dbConfig() {
        return new Properties();
    }

    @Profile("postgres")
    @Bean()
    public DataSource dataSource(@Qualifier("db-config") Properties properties, Environment env) {
        if (properties.get("connectionInitSql") == null && properties.get("connection-init-sql") == null) {
            String set_tz = "SET TIME ZONE '" + ZoneId.systemDefault().getId() + "';";
            properties.put("connectionInitSql", set_tz);
        }
        HikariDataSource ds = new HikariDataSource(new HikariConfig(properties));
        return ds;
    }

//    @Bean("sharedTxManager")
//    @Primary
//    public PlatformTransactionManager timescaleTxManager(@Qualifier("sharedDataSource") DataSource dataSource) {
//        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
//        transactionManager.setDataSource(dataSource);
//        return transactionManager;
//    }

}
