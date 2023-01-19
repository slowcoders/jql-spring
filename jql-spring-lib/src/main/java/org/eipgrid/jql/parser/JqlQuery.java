package org.eipgrid.jql.parser;

import org.eipgrid.jql.schema.JQColumn;
import org.eipgrid.jql.schema.JQSchema;
import org.eipgrid.jql.jdbc.JQResultMapping;
import org.eipgrid.jql.schema.JQType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JqlQuery extends TableFilter {

    private final ArrayList<JQResultMapping> columnGroupMappings = new ArrayList<>();
    private int cntMappingAlias;

    private boolean selectAuto;

    public JqlQuery(JQSchema schema) {
        super(schema, "t_0");
    }

    public void setSelectedProperties(String[] keys) {
        selectAuto = (keys == null || keys.length == 0);
        if (selectAuto) return;

        for (int i = 0; i < keys.length; i ++) {
            String k = keys[i].trim();
            this.addSelection(k.trim());
        }
    }

    private void addSelection(String key) {
        TableFilter scope = this;
        for (int p; (p = key.indexOf('.')) > 0; ) {
            JQSchema schema = scope.getSchema();
            if (schema != null && schema.hasColumn(key)) {
                break;
            }
            String token = key.substring(0, p);
            scope = scope.makeSubNode(token, JqlParser.NodeType.Entity).asTableFilter();
            key = key.substring(p + 1);
        }
        switch (key.charAt(0)) {
            case '<':
            case '[':
                String[] keys = key.substring(1, key.length()-1).split("\\s");
                for (String k : keys) {
                    scope.addSelectedColumn(k);
                }
                return;
            default:
        }
        if (scope.getSchema().getEntityJoinBy(key) != null) {
            scope = scope.makeSubNode(key, JqlParser.NodeType.Leaf).asTableFilter();
            key = "*";
        }
        scope.addSelectedColumn(key);
    }

    public JqlQuery getRootNode() {
        return this;
    }

    @Override
    public boolean isArrayNode() {
        return true;
    }

    public List<JQResultMapping> getResultMappings() {
        if (columnGroupMappings.size() == 0) {
            gatherColumnMappings(columnGroupMappings);
        }
        return columnGroupMappings;
    }

    String createUniqueMappingAlias() {
        cntMappingAlias ++;
        if (cntMappingAlias < 10) {
            return "t_" + cntMappingAlias;
        } else {
            return "t" + cntMappingAlias;
        }
    }


    boolean isSelectAuto() {
        return selectAuto;
    }
//    public List<JQColumn> resolveSelectedColumns(TableFilter tableFilter) {
//        if (!selectAuto) return Collections.EMPTY_LIST;
//
//        JQSchema schema = tableFilter.getSchema();
//        List<JQColumn> columns = tableFilter.getSchema().getReadableColumns();
//
//        Set<JQColumn> hiddenKeys = tableFilter.getHiddenForeignKeys();
//        if (!hiddenKeys.isEmpty()) {
//            ArrayList<JQColumn> columns2 = new ArrayList<>();
//            for (JQColumn column : tableFilter.getSelectedColumns()) {
//                if (hiddenKeys.contains(column)) continue;
//                columns2.add(column);
//            }
//            columns = columns2;
//        }
//
//        return columns;
//    }
}
