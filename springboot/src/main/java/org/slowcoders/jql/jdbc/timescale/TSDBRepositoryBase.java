package org.slowcoders.jql.jdbc.timescale;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.jql.JQLService;
import org.slowcoders.jql.jpa.JPARepositoryBase;
import org.slowcoders.jql.util.ClassUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TSDBRepositoryBase<ENTITY, ID> extends JPARepositoryBase<ENTITY, ID> {

    private final String timeKeyColumnName;
    private static final String SUFFIX_CONT_AGG = "_ts_agg";
    private static final String SUFFIX_HOURLY_VIEW = "_hourly";
    private static final String SUFFIX_DAILY_VIEW = "_daily";
    private final JdbcTemplate jdbc;
    private final ObjectMapper om;
    private final ArrayList<Field> dbColumns;

    public TSDBRepositoryBase(JQLService service, String timeKeyColumnName) {
        super(service);
        this.jdbc = service.getJdbcTemplate();
        this.om = service.getObjectMapper();
        this.timeKeyColumnName = timeKeyColumnName;
        this.dbColumns = ClassUtils.getInstanceFields(getEntityType(), true);

        this.initializeTSDB();
    }

    public String getTimeKeyColumnName() {
        return timeKeyColumnName;
    }

    public void initializeTSDB() {
        String sql = build_init_timescale(2);
        jdbc.execute(sql);
        try {
            sql = "SELECT 1 FROM " + super.getTableName() + SUFFIX_DAILY_VIEW + " limit 1";
            List<Map<String, Object>> res = jdbc.queryForList(sql);
            if (res.size() >= 0) return;
        } catch (Exception e) {
        }
        remove_down_sampling_view();
        // 6주를 기본 저장 간격으로 설정한다.
        sql = build_auto_down_sampling_view(7 * 6);
        jdbc.execute(sql);
    }

    private void execute_silently(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void remove_down_sampling_view() {
        String tableName = super.getTableName();
        String aggView = tableName + SUFFIX_CONT_AGG;
        execute_silently("SELECT remove_continuous_aggregate_policy('" + aggView + "');");
        execute_silently("DROP MATERIALIZED VIEW " + aggView + " cascade");
    }

    private void refresh_aggregation(Timestamp start, Timestamp end) {
        String tableName = super.getTableName();
        String aggView = tableName + SUFFIX_CONT_AGG;

        String sql = "CALL refresh_continuous_aggregate('" + aggView + "', NULL, DATE_TRUNC('hour', now()));";
        jdbc.execute(sql);
    }

    protected String build_init_timescale(int hours) {
        String sql = String.format("SELECT create_hypertable('%s', '%s', " +
                        "if_not_exists => TRUE, migrate_data => true, " +
                        "chunk_time_interval => interval '%d hour')",
                super.getTableName(), timeKeyColumnName, hours);
        return sql;
    }


    Aggregate.Type getAggregationType(Field column) {
        Aggregate c = column.getAnnotation(Aggregate.class);
        if (c != null) {
            return c.value();
        }
        Class type = column.getType();
        if (type == Float.class || type == Double.class ||
                type == float.class || type == double.class) {
            return Aggregate.Type.Mean;
        }
        return Aggregate.Type.None;
    }

    private StringBuilder replaceTrailingComma(StringBuilder sb, String text) {
        int len = sb.length();
        while (len > 0) {
            char ch = sb.charAt(--len);
            if (ch <= ' ') continue;
            if (ch == ',') break;
        }
        sb.setLength(len);
        sb.append(text);
        return sb;
    }

    protected String build_auto_down_sampling_view(int retention_days) {
        StringBuilder sb = new StringBuilder();
        String tableName = super.getTableName();
        String aggView = tableName + SUFFIX_CONT_AGG;

        sb.append("CREATE MATERIALIZED VIEW IF NOT EXISTS ").append(aggView);
        sb.append("\n\tWITH (timescaledb.continuous)\nAS SELECT\n\t");
        sb.append("time_bucket('1 hour', " + timeKeyColumnName + ") AS time_h,\n\t");


        for (String col : super.getPrimaryKeys()) {
            if (!col.equals(timeKeyColumnName)) {
                sb.append(col).append(",\n\t");
            }
        }

        ArrayList<Field> accColumns = new ArrayList<>();
        for (Field col : dbColumns) {
            String col_name = col.getName();
            ensureColumnName(col_name, col);
            switch (getAggregationType(col)) {
                case Sum: {
                    String ss = "min({0}) as {0}_min,\n\t" +
                            "max({0}) as {0}_max,\n\t" +
                            "first({0}, {1}) as {0}_first,\n\t" +
                            "last({0}, {1}) as {0}_last,\n\t";
                    ss = ss.replace("{0}", col_name).
                            replace("{1}", timeKeyColumnName);
                    sb.append(ss);
                    accColumns.add(col);
                    break;
                }
                case Mean: {
                    String ss = "avg(" + col_name + ") as " + col_name + ",\n\t";
                    sb.append(ss);
                    break;
                }
                case None:
                    break;
            }
        }

        replaceTrailingComma(sb, "\nFROM ").append(super.getTableName());
        sb.append("\nGROUP BY ");
        for (String col : super.getPrimaryKeys()) {
            if (col.equals(timeKeyColumnName)) {
                sb.append("time_h, ");
            } else {
                sb.append(col).append(", ");
            }
        }
        replaceTrailingComma(sb, ";\n\n");

        String retention_interval = retention_days <= 0 ? "NULL" : "INTERVAL '" + retention_days + " day'";
        sb.append("SELECT add_continuous_aggregate_policy('" + aggView + "',\n");
        sb.append("start_offset => " + retention_interval + ",\n");
        sb.append("end_offset => INTERVAL '1 hour',\n" +
                "schedule_interval => INTERVAL '1 hour');\n\n");

        if (false && retention_days > 0) {
            // 설치 버전 문제인지 아래 함수 호출에 실패.
            sb.append("SELECT add_drop_chunks_policy('" + aggView + "', " +
                    retention_interval + ", cascade_to_materializations=>FALSE);\n\n");
        }

        sb.append("CREATE OR REPLACE VIEW " + (tableName + SUFFIX_HOURLY_VIEW) + " AS\nSELECT\n\t");
        if (accColumns.size() == 0) {
            sb.append("* FROM \n").append(aggView).append(";\n\n");
        } else {
            for (String col : super.getPrimaryKeys()) {
                if (col.equals(timeKeyColumnName)) {
                    sb.append("time_h,\n\t");
                } else {
                    sb.append(col).append(",\n\t");
                }
            }
            for (Field col : dbColumns) {
                switch (getAggregationType(col)) {
                    case Sum: {
                        String fmt =
                                "(case when {0}_last >= least({0}_first, lag({0}_last) over _w) then\n" +
                                        "           {0}_max - least({0}_min, lag({0}_last) over _w)\n" +
                                        "      else {0}_max - least({0}_first, lag({0}_last) over _w) + ({0}_last - {0}_min)\n" +
                                        " end) as {0},\n\t";
                        fmt = fmt.replace("{0}", col.getName());
                        sb.append(fmt);
                        break;
                    }
                    case Mean: {
                        sb.append(col.getName()).append(",\n\t");
                        break;
                    }
                }
            }
            replaceTrailingComma(sb, "\nFROM ").append(aggView);
            sb.append("\nWINDOW _w AS(partition by ");
            for (String col : super.getPrimaryKeys()) {
                if (!col.equals(timeKeyColumnName)) {
                    sb.append(col).append(", ");
                }
            }
            replaceTrailingComma(sb, " ORDER BY time_h);\n");
        }

        sb.append("\nCREATE OR REPLACE VIEW " + (tableName + SUFFIX_DAILY_VIEW) + " AS\nSELECT\n\t");
        sb.append("time_bucket('1 day', time_h) AS time_d,\n\t");
        for (String col : super.getPrimaryKeys()) {
            if (!col.equals(timeKeyColumnName)) {
                sb.append(col).append(",\n\t");
            }
        }
        for (Field col : dbColumns) {
            switch (getAggregationType(col)) {
                case Sum: {
                    sb.append("sum(" + col.getName() + ") as " + col.getName() + ",\n\t");
                    break;
                }
                case Mean: {
                    sb.append("avg(" + col.getName() + ") as " + col.getName() + ",\n\t");
                    break;
                }
            }
        }
        replaceTrailingComma(sb, "\n");
        sb.append("FROM " + (tableName + SUFFIX_HOURLY_VIEW) + "\nGROUP BY ");
        for (String col : super.getPrimaryKeys()) {
            if (!col.equals(timeKeyColumnName)) {
                sb.append(col).append(", ");
            }
        }
        sb.append("time_d;\n");
        return sb.toString();
    }

    String createBulkInsertSQL(boolean ignoreConflict) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(super.getTableName()).append("(");
        for (Field col : dbColumns) {
            sb.append(col.getName()).append(", ");
        }
        replaceTrailingComma(sb, ") VALUES (");
        for (Field col : dbColumns) {
            sb.append("?,");
        }
        replaceTrailingComma(sb, ")");
        if (ignoreConflict) {
            sb.append("\nON CONFLICT DO NOTHING");
        }
        return sb.toString();
    }

    private void ensureColumnName(String col_name, Field col) {
        Column column = col.getAnnotation(Column.class);
        if (column != null) {
            String name = column.name();
            if (name.length() > 0 && !name.equalsIgnoreCase(col_name)) {
                throw new RuntimeException("Timescale repository does not support column name conversion.");
            }
        }
    }

    public int insertBulk(List<Map<String, Object>> entities) {
        BatchUpsert batch = new BatchUpsert(entities, dbColumns, createBulkInsertSQL(true));
        jdbc.batchUpdate(batch.getSql(), batch);
        return entities.size();
    }

    public static class BatchUpsert<ID> implements BatchPreparedStatementSetter {
        private final Map<String, Object>[] entities;
        private final String sql;
        private final ArrayList<Field> columns;
        private List<Map<String, Object>> generatedKeys;

        BatchUpsert(List<Map<String, Object>> entities, ArrayList<Field> dbColumns, String sql) {
            this.entities = entities.toArray(new Map[entities.size()]);
            this.sql = sql;
            this.columns = dbColumns;
        }

        public String getSql() {
            return sql;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Map<String, Object> entity = entities[i];
            ResultSetMetaData md = ps.getMetaData();
            int idx = 0;
            for (Field f : columns) {
                Object value = entity.get(f.getName());
                ps.setObject(++idx, value);
            }
        }

        @Override
        public int getBatchSize() {
            return entities.length;
        }
    }
}