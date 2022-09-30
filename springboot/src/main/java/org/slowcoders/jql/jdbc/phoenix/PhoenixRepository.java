package org.slowcoders.jql.jdbc.phoenix;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.jql.jdbc.BatchUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//@Repository
public class PhoenixRepository extends PhoenixJdbcHelper {

    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private Connection connection;

    @Autowired
    public PhoenixRepository(@Qualifier("phoenixDatasource")DataSource dataSource,
                             @Qualifier("phoenixTxManager")PlatformTransactionManager txManager) {
        super(new JdbcTemplate(dataSource), (Class)null, "eip2.solar_inverter");
        this.transactionTemplate = new TransactionTemplate(txManager);
        this.objectMapper = new ObjectMapper();
        try {
            Class.forName("org.apache.phoenix.queryserver.client.Driver");
            this.connection = DriverManager.getConnection("jdbc:phoenix:thin:url=http://localhost:18765;serialization=PROTOBUF");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }


    public void upsertAll(ArrayList<Map<String, Object>> entities) {
        try {
            connection.setAutoCommit(true);
            Statement statement = connection.createStatement();
            for (Map<String, Object> entity : entities) {
                //getSchema().autoArrangeColumns(entity, objectMapper);
                String sql = super.insert(entity, false);
                statement.executeUpdate(sql);
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    //@Transactional("phoenixTxManager")
    public void upsertAll2(List<Map<String, Object>> entities) {
        BatchUpsert batch = super.prepareInsert(entities);
        jdbc.batchUpdate(batch.getSql(), batch);
    }

    public void createTable() {
        jdbc.execute("DROP TABLE " + getSchema().getTableName());

        String ddl = this.getSchema().generateDDL();
        jdbc.execute(ddl);
    }
}
