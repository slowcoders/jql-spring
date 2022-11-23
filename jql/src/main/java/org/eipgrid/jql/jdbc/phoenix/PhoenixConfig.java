package org.eipgrid.jql.jdbc.phoenix;

//import com.amazon.hive.jdbc.HS2DataSource;
//import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
public class PhoenixConfig {

    @Bean(name="phoenixDatasource")
    public DataSource phoenixDataSource() {
        boolean use_thin_server = true;
        HikariDataSource dataSource = new HikariDataSource();
        if (use_thin_server) {
            dataSource.setDriverClassName("org.apache.phoenix.queryserver.client.Driver");
            dataSource.setAutoCommit(false);
            dataSource.setJdbcUrl("jdbc:phoenix:thin:url=http://ec2-3-35-209-65.ap-northeast-2.compute.amazonaws.com:8765;serialization=PROTOBUF");
            //dataSource.setJdbcUrl("jdbc:phoenix:thin:url=http://localhost:18765;serialization=PROTOBUF");
        }
        else {
            dataSource.setDriverClassName("org.apache.phoenix.jdbc.PhoenixDriver");
            dataSource.setJdbcUrl("jdbc:phoenix:localhost:2181:/hbase-unsecure");
        }
        return dataSource;

        // hbase.zookeeper.quorum: ip-172-31-14-239.ap-northeast-2.compute.internal
    }

    @Bean("phoenixTxManager")
    public PlatformTransactionManager phoenixTxManager() throws Exception {
        return new DataSourceTransactionManager(phoenixDataSource());
    }


}
