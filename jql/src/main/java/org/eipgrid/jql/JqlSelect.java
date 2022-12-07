package org.eipgrid.jql;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JqlSelect {
    private Sort sort;
    private boolean selectAll;
    ArrayList<String> properties = new ArrayList<>();
    private int offset;
    private int limit;
    private static JqlSelect selectAny = new JqlSelect(Sort.unsorted(), 0, 0);
    private static char SORT_OPTION_SEPARATOR = '/';

    private JqlSelect(Sort sort, int offset, int limit) {
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
    }

    private JqlSelect(String[] properties, int offset, int limit) {
        this.offset = offset;
        this.limit  = limit;
        ArrayList<RankedOrder> orders = new ArrayList<>();
        for (String name : properties) {
            addColumn(name, orders);
        }
        orders.sort((o1, o2) -> {
            return o1.rank() - o2.rank();
        });
        this.sort = Sort.by((List)orders);
    }

    private JqlSelect(List<String> properties, int offset, int limit) {
        this.offset = offset;
        this.limit  = limit;
        ArrayList<RankedOrder> orders = new ArrayList<>();
        for (String name : properties) {
            addColumn(name, orders);
        }
        orders.sort((o1, o2) -> {
            return o1.rank() - o2.rank();
        });
        this.sort = Sort.by((List)orders);
    }

    public static JqlSelect by(Sort sort, int offset, int limit) {
        return new JqlSelect(sort, offset, limit);
    }

    public static JqlSelect by(String[] properties, int offset, int limit) {
        return new JqlSelect(properties, offset, limit);
    }

    public static JqlSelect by(String commaSeperatedProperties, int offset, int limit) {
        String[] properties = commaSeperatedProperties.split(",");
        return new JqlSelect(properties, offset, limit);
    }

    public static JqlSelect by(List<String> properties, int offset, int limit) {
        return new JqlSelect(properties, offset, limit);
    }

    public static JqlSelect selectAny() {
        return selectAny;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public List<JqlColumn> getSelectedColumns(JqlSchema schema) {
        List<JqlColumn> extraColumns = selectAll ? schema.getReadableColumns() : schema.getPKColumns();
        if (this.properties.isEmpty()) {
            return extraColumns;
        }
        ArrayList<JqlColumn> columns = new ArrayList<>();
        for (String name : this.properties) {
            JqlColumn column = schema.getColumn(name);
            columns.add(column);
        }
        for (JqlColumn col : extraColumns) {
            if (columns.indexOf(col) < 0) {
                columns.add(col);
            }
        }
        return columns;
    }

    public Sort getSort() {
        return this.sort;
    }

    private void addColumn(String property, ArrayList<RankedOrder> orders) {

        property = property.trim();
        if (!this.selectAll && property.equals("*")) {
            this.selectAll = true;
            return;
        }
        int idx = property.indexOf(SORT_OPTION_SEPARATOR);
        if (idx > 0) {
            RankedOrder order = createOrder(property, idx, orders.size());
            orders.add(order);
            property = order.getProperty();
        }

        if (!this.selectAll) {
            addNormalColumn(property);
        }
    }

    static RankedOrder createOrder(String property, int idx, int minorRank) {
        if (idx <= 0) {
            throw new IllegalArgumentException("invalid ordered column expression: " + property);
        }
        String name = property.substring(0, idx);
        int rank = 0;
        boolean ascend = true;
        if (property.length() > ++idx) {
            int ch = property.charAt(idx);
            if (ch == '-') {
                ascend = false;
                idx ++;
            }
            if (property.length() > idx) {
                rank = Integer.parseInt(property.substring(idx));
            }
        }
        RankedOrder order = new RankedOrder(name, ascend, rank * 100 + minorRank);
        return order;
    }

    private boolean addNormalColumn(String property) {
        if (properties.indexOf(property) < 0) {
            properties.add(property);
            return true;
        }
        return false;
    }

    public static Sort buildSort(String commaSeperatedProperties) {
        String[] properties = commaSeperatedProperties.split(",");
        return buildSort(properties);
    }

    public static Sort buildSort(String[] properties) {
        if (properties == null || properties.length == 0) {
            return Sort.unsorted();
        }
        ArrayList<Sort.Order> orders = new ArrayList<>();
        for (String name : properties) {
            int idx = name.indexOf(SORT_OPTION_SEPARATOR);
            orders.add(createOrder(name, idx, 0));
        }
        return Sort.by(orders);
    }

    static class RankedOrder extends Sort.Order {
        int rank;
        public RankedOrder(String property, boolean ascend, int rank) {
            super(ascend ? Sort.Direction.ASC : Sort.Direction.DESC, property);
            this.rank = rank;
        }

        final int rank() {
            return rank;
        }
    }

}
