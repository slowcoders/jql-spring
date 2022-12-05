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

    private JqlSelect(Sort sort, int offset, int limit) {
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
    }

    private JqlSelect(String[] properties, int offset, int limit) {
        this.offset = offset;
        this.limit  = limit;
        ArrayList<Sort.Order> orders = new ArrayList<>();
        for (String name : properties) {
            addColumn(name, orders);
        }
        this.sort = Sort.by(orders);
    }

    private JqlSelect(List<String> properties, int offset, int limit) {
        this.offset = offset;
        this.limit  = limit;
        ArrayList<Sort.Order> orders = new ArrayList<>();
        for (String name : properties) {
            addColumn(name, orders);
        }
        this.sort = Sort.by(orders);
    }

    public static JqlSelect by(Sort sort, int offset, int limit) {
        return new JqlSelect(sort, offset, limit);
    }

    public static JqlSelect by(String[] properties, int offset, int limit) {
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

    public boolean shouldSelectAll() {
        return this.selectAll;
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

    public Sort getOrders() {
        return this.sort;
    }

    private boolean addColumn(String property, ArrayList<Sort.Order> orders) {

        property = property.trim();
        char firstCh = property.charAt(0);
        switch (firstCh) {
            case '+':
            case '-':
                Sort.Order order = createOrder(property);
                orders.add(order);
                property = order.getProperty();
                break;
            case '*':
                if (property.length() == 1) {
                    this.selectAll = true;
                } else {
                    throw new IllegalArgumentException("invalid property name: [" + property + "]");
                }
                return true;
        }

        return addNormalColumn(property);
    }

    private boolean addNormalColumn(String property) {
        if (properties.indexOf(property) < 0) {
            properties.add(property);
            return true;
        }
        return false;
    }

    public static Sort buildSort(String[] properties) {
        if (properties == null || properties.length == 0) {
            return Sort.unsorted();
        }
        ArrayList<Sort.Order> orders = new ArrayList<>();
        for (String name : properties) {
            orders.add(createOrder(name));
        }
        return Sort.by(orders);
    }

    public static Sort.Order createOrder(String property) {
        char firstCh = property.charAt(0);
        boolean ascend;
        switch (firstCh) {
            case '+':
            case '-':
                property = property.substring(1).trim();
                ascend = firstCh == '+';
                break;
            default:
                if (!Character.isJavaIdentifierStart(firstCh)) {
                    throw new IllegalArgumentException("invalid property name: [" + property + "]");
                }
                ascend = true;
        }
        return new Sort.Order(ascend ? Sort.Direction.ASC : Sort.Direction.DESC, property);
    }

//    public boolean createOrder(String property, boolean ascend) {
//        for (Sort.Order o : this.orders) {
//            if (o.getProperty().equals(property)) return false;
//        }
//        Sort.Order order = new Sort.Order(ascend ? Sort.Direction.ASC : Sort.Direction.DESC, property);
//        orders.add(order);
//        addNormalColumn(property);
//        return true;
//    }

}
