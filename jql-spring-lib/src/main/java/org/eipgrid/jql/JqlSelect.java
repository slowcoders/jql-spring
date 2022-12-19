package org.eipgrid.jql;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class JqlSelect {
    private final String[] columns;
    private Sort sort;
    private int offset;
    private int limit;

    public static final String ALL_PROPERTIES = "*";
    public static final String PRIMARY_KEYS = "!";

    public static final JqlSelect Whole = new JqlSelect(null, null, 0, 0);
    public static final JqlSelect NotAtAll = new JqlSelect(new String[0], null, 0, 0);

    protected JqlSelect(String[] columns, Sort sort, int offset, int limit) {
        this.columns = columns;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
    }

    public static JqlSelect by(String[] columns, Sort sort, int offset, int limit) {
        return new JqlSelect(columns, sort, offset, limit);
    }

    public static JqlSelect by(String[] columns, String[] sort, int offset, int limit) {
        return by(columns, buildSort(sort), offset, limit);
    }

    public static JqlSelect by(String columns, String sort, int offset, int limit) {
        String[] _columns = splitPropertyKeys(columns);
        return by(_columns, parseSort(sort), offset, limit);
    }

    public static String[] splitPropertyKeys(String columns) {
        if (columns != null) {
            columns = columns.trim();
            if (columns.length() > 0) {
                return columns.split(",");
            }
            return NotAtAll.columns;
        }
        return null;
    }

    public String[] getAttributeNames() {
        return this.columns;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public Sort getSort() {
        return this.sort;
    }

    public static Sort.Order createOrder(String column) {
        char first_ch = column.charAt(0);
        boolean ascend = first_ch != '-';
        String name = (ascend && first_ch != '+') ? column : column.substring(1);
        return ascend ? Sort.Order.asc(name) : Sort.Order.desc(name);
    }

    public static Sort parseSort(String commaSeperatedProperties) {
        String[] properties = splitPropertyKeys(commaSeperatedProperties);
        return buildSort(properties);
    }

    public static Sort buildSort(String[] orders) {
        if (orders == null || orders.length == 0) {
            return Sort.unsorted();
        }
        ArrayList<Sort.Order> _orders = new ArrayList<>();
        for (String column : orders) {
            Sort.Order order = createOrder(column);
            _orders.add(order);
        }
        return Sort.by(_orders);
    }

}
