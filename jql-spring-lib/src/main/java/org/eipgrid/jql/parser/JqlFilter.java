package org.eipgrid.jql.parser;

import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.schema.QResultMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JqlFilter extends TableFilter {

    private final ArrayList<QResultMapping> columnGroupMappings = new ArrayList<>();
    private int cntMappingAlias;

    private boolean selectAuto;

    public JqlFilter(QSchema schema) {
        super(schema, "t_0");
    }

    public static <ID> JqlFilter of(QSchema schema, ID id) {
        JqlFilter filter = new JqlFilter(schema);
        filter.getPredicateSet().add(PredicateFactory.IS.createPredicate(schema.getPKColumns().get(0).getPhysicalName(), id));
        return filter;
    }

    public static <ID> JqlFilter of(QSchema schema, Collection<ID> idList) {
        JqlFilter filter = new JqlFilter(schema);
        filter.getPredicateSet().add(PredicateFactory.IS.createPredicate(schema.getPKColumns().get(0).getPhysicalName(), idList));
        return filter;
    }

    public void setSelectedProperties(String[] keys) {
        selectAuto = (keys == null || keys.length == 0);
        if (selectAuto) {
            this.addSelection("*");
            return;
        }

        for (int i = 0; i < keys.length; i ++) {
            String k = keys[i].trim();
            this.addSelection(k.trim());
        }
    }

    private void addSelection(String key) {
        TableFilter scope = this;
        for (int p; (p = key.indexOf('.')) > 0; ) {
            QSchema schema = scope.getSchema();
            if (schema != null && schema.hasColumn(key)) {
                break;
            }
            String token = key.substring(0, p);
            scope = scope.makeSubNode(token, JqlParser.NodeType.Entity).asTableFilter();
            key = key.substring(p + 1);
        }
//        switch (key.charAt(0)) {
//            case '<':
//            case '[':
//                String[] keys = key.substring(1, key.length()-1).split("\\s");
//                for (String k : keys) {
//                    scope.addSelectedColumn(k);
//                }
//                return;
//            default:
//        }
        if (scope.getSchema().getEntityJoinBy(key) != null) {
            scope = scope.makeSubNode(key, JqlParser.NodeType.Leaf).asTableFilter();
            key = "*";
        }
        scope.addSelectedColumn(key);
    }

    public JqlFilter getRootNode() {
        return this;
    }

    @Override
    public boolean isArrayNode() {
        return true;
    }

    public List<QResultMapping> getResultMappings() {
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
