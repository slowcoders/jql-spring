package org.eipgrid.jql;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;

public class JQSelect {
    private final String[] columns;
    private Sort sort;
    private int offset;
    private int limit;

    public static final String ALL_PROPERTIES = "*";
    public static final String PRIMARY_KEYS = "!";

    public static final JQSelect Whole = new JQSelect(null, null, 0, 0);
    public static final JQSelect NotAtAll = new JQSelect(new String[0], null, 0, 0);

    protected JQSelect(String[] columns, Sort sort, int offset, int limit) {
        this.columns = columns;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
    }

    public static JQSelect by(String[] attributes, Sort sort, int offset, int limit) {
        return new JQSelect(attributes, sort, offset, limit);
    }

    public static JQSelect by(String[] attributes, String[] sort, int offset, int limit) {
        return by(attributes, buildSort(sort), offset, limit);
    }

    public static JQSelect by(String attributes, String sort, int offset, int limit) {
        String[] _columns = splitPropertyKeys(attributes);
        return by(_columns, parseSort(sort), offset, limit);
    }

    public static String[] splitPropertyKeys(String attributes) {
        if (attributes != null) {
            attributes = attributes.trim();
            if (attributes.length() > 0) {
                String[] cols = attributes.split("\\s*,\\s*");
                return cols;
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
