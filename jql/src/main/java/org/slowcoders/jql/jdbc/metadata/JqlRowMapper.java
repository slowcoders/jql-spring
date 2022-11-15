package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.parser.JqlResultMapping;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JqlRowMapper implements RowMapper<KVEntity> {
    private final List<JqlResultMapping> resultMappings;
    private JqlColumn[] mappedColumns;
    private JqlResultMapping[] mappedMappings;
    private int columnCount;

    public JqlRowMapper(List<JqlResultMapping> schema) {
        this.resultMappings = schema;
    }

    @Override
    public KVEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (mappedColumns == null) {
            initMappedColumns(rs);
        }

        KVEntity baseEntity = new KVEntity();
        KVEntity subEntity = baseEntity;
        JqlResultMapping curr_node = null;
        for (int idxColumn = 1; idxColumn <= columnCount; idxColumn++) {
            JqlResultMapping outNode = mappedMappings[idxColumn];
            if (outNode != curr_node) {
                String[] fieldPath = outNode.getJsonPath();
                subEntity = baseEntity;
                for (int i = 0; i < fieldPath.length; i++) {
                    subEntity = makeSubEntity(subEntity, fieldPath[i]);
                }
            }
            JqlColumn jqlColumn = mappedColumns[idxColumn];
            Object value = getColumnValue(rs, idxColumn);

            String fieldName = jqlColumn.getJsonKey();
            KVEntity entity = subEntity;
            for (int p; (p = fieldName.indexOf('.')) > 0; ) {
                entity = makeSubEntity(entity, fieldName.substring(0, p));
                fieldName = fieldName.substring(p + 1);
            }
            entity.putIfAbsent(fieldName, value);

        }
        return baseEntity;
    }

    private void initMappedColumns(ResultSet rs) throws SQLException {

        String currTableName = null;
        String currDbSchema = null;
        int idxFetch = 0;

        ResultSetMetaData rsmd = rs.getMetaData();
        this.columnCount = rsmd.getColumnCount();

        this.mappedColumns = new JqlColumn[columnCount+1];
        this.mappedMappings = new JqlResultMapping[columnCount+1];

        JqlSchema jqlSchema = null;
        JqlResultMapping outNode = null;
        for (int idxColumn = 1; idxColumn <= columnCount; idxColumn++) {
            String column = JdbcUtils.lookupColumnName(rsmd, idxColumn);
            String tableName = rsmd.getTableName(idxColumn);
            String dbSchema = rsmd.getSchemaName(idxColumn);
            if (!tableName.equals(currTableName) || !dbSchema.equals(currDbSchema)) {
                outNode = resultMappings.get(idxFetch ++);
                jqlSchema = outNode.getSchema();
                currTableName = tableName;
                currDbSchema = dbSchema;
            }
            JqlColumn jqlColumn = jqlSchema.getColumn(column);
            this.mappedMappings[idxColumn] = outNode;
            this.mappedColumns[idxColumn] = jqlColumn;
        }
    }

    private KVEntity makeSubEntity(KVEntity entity, String key) {
        KVEntity subEntity = (KVEntity) entity.get(key);
        if (subEntity == null) {
            subEntity = new KVEntity();
            entity.put(key, subEntity);
        }
        return subEntity;
    }

    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }



}
