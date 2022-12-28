package org.eipgrid.jql.jdbc.metadata;

import com.zaxxer.hikari.HikariDataSource;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.json.JsonJql;
import org.eipgrid.jql.util.AttributeNameConverter;
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

        JdbcSchemaLoader mp = new JdbcSchemaLoader(ds, AttributeNameConverter.defaultConverter);
        for (String dbSchema : mp.getDBSchemas()) {
            List<String> tableNames = mp.getTableNames(dbSchema);
            ArrayList<JqlSchema> jqlSchemas = new ArrayList<>();
            for (String tableName : tableNames) {
                jqlSchemas.add(mp.loadSchema(dbSchema + '.' + tableName, null));
            }

            for (JqlSchema schema : jqlSchemas) {
                ((JdbcSchema)schema).dumpJPAEntitySchema();
            }
            for (JqlSchema schema : jqlSchemas) {
                String ddl = "";//schema.generateDDL();
//                System.out.println(ddl);
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