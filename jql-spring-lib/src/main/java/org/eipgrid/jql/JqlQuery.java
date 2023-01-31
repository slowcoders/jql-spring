package org.eipgrid.jql;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.schema.QResultMapping;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * 1) select 가 지정되지 않으면, 쿼리 문에 사용된 Entity 들의 Property 들을 검색 결과에 포함시킨다.
 * 2) select 를 이용하여 검색 결과에 포함할 Property 들을 명시할 수 있다.
 *    - Joined Property 명만 명시된 경우, 해당 Joined Entity 의 모든 Property 를 선택.
 */
@Getter
public class JqlQuery {

    private final JqlEntityStore<?> table;
    private final String[] select;
    private final JqlFilter filter;


    private Sort sort;
    private int offset;
    private int limit;


    /*package*/ String executedQuery;
    /*package*/ Object extraInfo;

    public static String[] All = new String[] { "*" };

    public static String[] PrimaryKeys = new String[] { "0" };

    public static String[] Auto = new String[0];


    protected JqlQuery(JqlEntityStore<?> table, String[] select, JqlFilter filter) {
        this.table = table;
        this.select = select;
        this.filter = filter;
    }

    private JqlQuery(JqlEntityStore<?> table, String[] select, Map<String, Object> filter) {
        this(table, select, table.createFilter(filter));
    }

    public static JqlQuery of(JqlEntityStore<?> table, String[] select, Sort sort, int offset, int limit, Map<String, Object> filter) {
        JqlQuery query = new JqlQuery(table, select, filter);
        query.sort = sort;
        query.offset = offset;
        query.limit = limit;
        return query;
    }

    public static JqlQuery of(JqlEntityStore<?> table, String[] select, Map<String, Object> filter) {
        return new JqlQuery(table, select, filter);
    }
    
    public boolean needPagination() {
        return offset >= 0 && limit > 0;
    }

    public Response execute() {
        List<?> result = table.find(this, null);
        Response resp = new Response(result, filter);
        if (needPagination()) {
            resp.setProperty("totalElements", this.count());
        }
        return resp;
    }

    public long count() {
        return table.count(filter);
    }

    @Data
    public static class Request {
        private String select;
        private String sort;
        private Integer page;
        private Integer limit;

        @Schema(implementation = Object.class)
        private HashMap filter;

        public JqlQuery buildQuery(JqlEntityStore<?> table) {
            String[] _select = select == null ? null : parsePropertySelection(select);
            Sort _sort = parseSort(sort);
            int _limit = limit == null ? 0 : limit;
            int _page = page == null ? -1 : page;

            JqlQuery query = JqlQuery.of(table, _select, _sort, _page * _limit, _limit, filter);
            return query;
        }
    }

    @Getter
    public static class Response {

        private Map<String, Object> metadata;
        private Object content;

        @JsonIgnore
        private QResultMapping resultMapping;

        public Response(Object content, QResultMapping resultMapping) {
            this.content = content;
            this.resultMapping = resultMapping;
        }

        public void setProperty(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
        }
    }

    public static String[] parsePropertySelection(String select) {
        String[] keys = splitPropertyKeys(select);
        if (keys != null) {
            for (int i = 0; i < keys.length; i ++) {
                String k = keys[i];
                int p = k.lastIndexOf('.');
                int last_ch = ']';
                switch (k.charAt(p + 1)) {
                    case '<':
                        last_ch = '>';
                    case '[':
                        String base = k.substring(0, p + 1);
                        k = k.substring(p + 2);
                        while (true) {
                            int end = k.indexOf(last_ch);
                            if (end > 0) {
                                k = k.substring(0, end);
                            }
                            keys[i] = base + k;
                            if (end > 0) break;

                            k = keys[++i];
                        }
                }
            }
        }
        return keys;
    }


    public static String[] splitPropertyKeys(String keys) {
        if (keys != null) {
            keys = keys.trim();
            if (keys.length() > 0) {
                return keys.split("\\s*,\\s*");
            }
        }
        return null;
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
