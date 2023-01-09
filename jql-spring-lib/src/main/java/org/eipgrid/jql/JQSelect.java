package org.eipgrid.jql;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;

public class JQSelect {

    private final String[] keys;
    private Sort sort;
    private int offset;
    private int limit;

    public static final char ALL_PROPERTIES = '*';
    public static final char PRIMARY_KEYS = '0';

    public static final char COMPARED_PROPERTIES = '@';

    public static final char NOTHING = '_';

    public static final JQSelect Whole = new JQSelect(new String[] { "*" }, null, 0, 0);
    public static final JQSelect NotAtAll = new JQSelect(new String[0], null, 0, 0);

    protected JQSelect(String[] keys, Sort sort, int offset, int limit) {
        this.keys = keys == null ? Whole.keys : keys;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
    }

    public static JQSelect by(String[] keys, Sort sort, int offset, int limit) {
        return new JQSelect(keys, sort, offset, limit);
    }

    public static JQSelect by(String[] keys, String[] sort, int offset, int limit) {
        return by(keys, buildSort(sort), offset, limit);
    }

    public static JQSelect by(String keys, String sort, int offset, int limit) {
        String[] _columns = splitPropertyKeys(keys);
        return by(_columns, parseSort(sort), offset, limit);
    }

    public static String[] splitPropertyKeys(String keys) {
        if (keys != null) {
            keys = keys.trim();
            if (keys.length() > 0) {
                return keys.split("\\s*,\\s*");
            }
            return NotAtAll.keys;
        }
        return null;
    }

    public String[] getPropertyKeys() {
        return this.keys;
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
