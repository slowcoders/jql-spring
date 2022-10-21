package org.slowcoders.jql.jdbc.postgres;

import lombok.SneakyThrows;
import org.postgresql.jdbc.PgConnection;
import org.slowcoders.jql.AutoClearEntityCache;
import org.slowcoders.jql.AutoUpdateModifyTimestamp;
import org.slowcoders.jql.JQLService;
import org.slowcoders.jql.jpa.JPARepositoryBase;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateListener extends Thread {
    private final JQLService jqlService;
    private final JPARepositoryBase repository;
    private final PgConnection conn;
    private final ConnectionHolder connHolder;

    public UpdateListener(JQLService jqlService, String event, JPARepositoryBase repository) throws SQLException {
        this.jqlService = jqlService;
        this.conn = jqlService.getDataSource().getConnection().unwrap(PgConnection.class);
        this.connHolder = new ConnectionHolder(this.conn);
        this.connHolder.requested();
        this.repository = repository;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("LISTEN " + event);
        stmt.close();
    }

    public static <ID, ENTITY> void initAutoUpdateTrigger(JQLService service, String tablePath, JPARepositoryBase<ENTITY,ID> repository) {
        Class<?> entityType = repository.getEntityType();
        AutoUpdateModifyTimestamp autoUpdate = entityType.getAnnotation(AutoUpdateModifyTimestamp.class);
        AutoClearEntityCache autoClearCache = entityType.getAnnotation(AutoClearEntityCache.class);
        if (autoUpdate == null && autoClearCache == null) return;

        String colName = autoUpdate.column();
        String tableName = tablePath.replace('.', '_');
        String firstPrimaryKey = repository.getPrimaryKeys()[0];
        if (repository.getPrimaryKeys().length > 1) {
            throw new RuntimeException("can not listen update event on table with multi primary keys. ");
        }

        String sql = "CREATE OR REPLACE FUNCTION auto_update__${TABLE}__modify_time()\n" +
                "RETURNS TRIGGER AS $$\n" +
                "BEGIN\n" +
                "    NEW.${COLUMN} = now();\n";
        if (autoClearCache != null) {
            sql += "   PERFORM pg_notify('${TABLE}_updated', NEW.${PK}::text);\n";
        }
        sql +=  "    RETURN NEW;\n" +
                "END;\n" +
                "$$\n" +
                "LANGUAGE PLPGSQL;\n" +
                "CREATE OR REPLACE TRIGGER ${TABLE}__modify_time_trigger\n" +
                "    BEFORE UPDATE OR INSERT\n" +
                "    ON ${TABLE_PATH}\n" +
                "    FOR EACH ROW EXECUTE PROCEDURE auto_update__${TABLE}__modify_time();";
        sql = sql.replace("${TABLE}", tableName)
                .replace("${TABLE_PATH}", tablePath)
                .replace("${PK}", firstPrimaryKey)
                .replace("${COLUMN}", colName);

        try {
            service.getJdbcTemplate().execute(sql);
            if (autoClearCache != null) {
                new UpdateListener(service, tableName + "_updated", repository).start();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        while (true) {
            try {
                // issue a dummy query to contact the backend
                // and receive any pending notifications.
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                rs.close();
                stmt.close();

                org.postgresql.PGNotification notifications[] = conn.getNotifications();
                if (notifications != null) {
                    jqlService.getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                        @SneakyThrows
                        protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                            for (int i = 0; i < notifications.length; i++) {
                                Object id = repository.convertId(notifications[i].getParameter());
                                repository.clearEntityCache(id);
                                System.out.println("Got notification: " + notifications[i].getName() + ": " + id);
                            }
                        }
                    });
                }

                // wait a while before checking again for new
                // notifications
                Thread.sleep(500);
            } catch (SQLException sqle) {
                try {
                    if (conn.isClosed()) break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                sqle.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}