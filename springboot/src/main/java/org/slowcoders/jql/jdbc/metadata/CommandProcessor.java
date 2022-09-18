package org.slowcoders.jql.jdbc.metadata;

import com.zaxxer.hikari.HikariDataSource;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.json.JsonJql;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommandProcessor implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        String jdbcUrl = args[0]; // "jdbc:postgresql://0.0.0.127/postgres";
        String jdbcDriver = "org.postgresql.Driver";
        String userName = args[1];
        String password = args[2];

        DataSource ds = DataSourceBuilder.create()
                .driverClassName(jdbcDriver)
                .type(HikariDataSource.class)
                .url(jdbcUrl)
                .username(userName)
                .password(password)
                .build();

//        Connection conn = ds.getConnection();

        MetadataProcessor mp = new MetadataProcessor(ds, AttributeNameConverter.defaultConverter);
        for (String dbSchema : mp.getDBSchemas()) {
            List<String> tableNames = mp.getTableNames(dbSchema);
            ArrayList<JqlSchema> jqlSchemas = new ArrayList<>();
            for (String tableName : tableNames) {
                jqlSchemas.add(mp.loadSchema(dbSchema, tableName));
            }

            for (JqlSchema schema : jqlSchemas) {
                schema.dumpJPAEntitySchema();
            }
            for (JqlSchema schema : jqlSchemas) {
                String ddl = schema.generateDDL();
                System.out.println(ddl);
            }
            for (JqlSchema schema : jqlSchemas) {
                String ddl = JsonJql.createJoinJQL(schema);
                System.out.println(ddl);
            }
        }

        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        new CommandProcessor().run(args);
    }

}