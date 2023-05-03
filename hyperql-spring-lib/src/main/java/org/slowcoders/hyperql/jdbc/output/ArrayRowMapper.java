package org.slowcoders.hyperql.jdbc.output;

import org.slowcoders.hyperql.schema.QResultMapping;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.util.KVEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class ArrayRowMapper implements ResultSetExtractor<List<KVEntity>> {
    private final List<QResultMapping> resultMappings;

    public ArrayRowMapper(List<QResultMapping> rowMappings) {
        this.resultMappings = rowMappings;
    }

    @Override
    public List<KVEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {

        String[] columnNames = initMappedColumns(rs);
        int columnCount = columnNames.length;

        ArrayList<Object[]> rows = new ArrayList<>();
        while (rs.next()) {
            Object[] values = new Object[columnCount];
            for (int i = columnNames.length; --i >= 0;) {
                values[i] = rs.getObject(i+1);
            }
            rows.add(values);
        }
        KVEntity res = KVEntity.of("columnNames", columnNames);
        res.put("rows", rows);
        List<KVEntity> results = Collections.singletonList(res);
        return results;
    }

    private String[] initMappedColumns(ResultSet rs) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        String[] mappedColumns = new String[columnCount];

        int idxColumn = 0;
        for (QResultMapping mapping : resultMappings) {
            List<QColumn> columns = mapping.getSelectedColumns();
            if (columns.size() == 0) {
                continue;
            }
            String base = toJsonKey(mapping.getEntityMappingPath());
            for (QColumn column : columns) {
                mappedColumns[idxColumn++] = toJsonKey(base, column.getJsonKey());
            }
        }
        if (idxColumn != columnCount) {
            throw new RuntimeException("Something wrong!");
        }
        return mappedColumns;
    }

    private String toJsonKey(String baseKey, String key) {
        if (baseKey.length() == 0) return key;
        return baseKey + '.' + key;
    }

    private String toJsonKey(String[] mappingPath) {
        StringBuilder sb = new StringBuilder();
        for (String s : mappingPath) {
            if (sb.length() > 0) sb.append('.');
            sb.append(s);
        }
        return sb.toString();
    }

}
