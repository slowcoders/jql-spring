package org.eipgrid.jql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 1) select 가 지정되지 않으면, 쿼리 문에 사용된 Entity 들의 Property 들을 검색 결과에 포함시킨다.
 * 2) select 를 이용하여 검색 결과에 포함할 Property 들을 명시할 수 있다.
 *    - Joined Property 명만 명시된 경우, 해당 Joined Entity 의 모든 Property 를 선택.
 */
@Getter
public class JqlRequest {

    private final String[] select;

    private final Sort sort;
    private final int offset;
    private final int limit;

    private final Map<String, Object> filter;

    public JqlRequest(String[] select, Sort sort, int offset, int limit, Map<String, Object> filter) {
        this.select = select;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
        this.filter = filter;
    }

    @Data
    public static class Builder {
        private String select;
        private String sort;
        private Integer page;
        private Integer limit;

        @Schema(implementation = Object.class)
        HashMap filter;

        public JqlRequest build() {
            String[] _select = parseSelection(select);
            Sort _sort = parseSort(sort);
            int _limit = limit == null ? 0 : limit;
            int _page = page == null ? 0 : page;
            return new JqlRequest(_select, _sort, _limit, _page, filter);
        }

        public static String[] parseSelection(String select) {
            String[] keys = splitPropertyKeys(select);
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
                        for (int end = -1; end < 0; ) {
                            end = k.indexOf(last_ch);
                            if (end > 0) {
                                k = k.substring(0, end);
                            }
                            keys[i] = base + k;
                            k = keys[++i];
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

}
