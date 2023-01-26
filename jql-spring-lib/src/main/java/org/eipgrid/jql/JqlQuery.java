package org.eipgrid.jql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * 1) select 가 지정되지 않으면, 쿼리 문에 사용된 Entity 들의 Property 들을 검색 결과에 포함시킨다.
 * 2) select 를 이용하여 검색 결과에 포함할 Property 들을 명시할 수 있다.
 *    - Joined Property 명만 명시된 경우, 해당 Joined Entity 의 모든 Property 를 선택.
 */
@Getter
public class JqlQuery {

    private final JqlRepository<?> repository;
    private final String[] select;
    private final JqlFilter filter;

    private Sort sort;
    private int offset;
    private int limit;

    public static final char All = '*';
    public static final char PrimaryKeys = '0';
    public static final char Auto = '@';

    public static String[] AllProperties = new String[] { "*" };

    private JqlQuery(JqlRepository<?> repository, String[] select, JqlFilter filter) {
        this.repository = repository;
        this.select = select;
        this.filter = filter;
    }
    private JqlQuery(JqlRepository<?> repository, String[] select, Map<String, Object> filter) {
        this(repository, select, repository.buildFilter(filter));
    }

    public static <T> JqlQuery of(JqlRepository<?>  repository, String[] select, Sort sort, int offset, int limit, Map<String, Object> filter) {
        JqlQuery query = new JqlQuery(repository, select, filter);
        query.sort = sort;
        query.offset = offset;
        query.limit = limit;
        return query;
    }

    public static <T> JqlQuery of(JqlRepository<?> repository, String[] select, Map<String, Object> filter) {
        return new JqlQuery(repository, select, filter);
    }

    public static <T> JqlQuery of(JqlRepository<?> repository, Map<String, Object> filter) {
        return new JqlQuery(repository, AllProperties, filter);
    }

    public static <T, ID> JqlQuery of(JqlRepository<ID> repository, ID id) {
        return new JqlQuery(repository, AllProperties, JqlFilter.of(repository.getSchema(), id));
    }

    public static <T, ID> JqlQuery of(JqlRepository<ID> repository, Collection<ID> idList) {
        return new JqlQuery(repository, AllProperties, JqlFilter.of(repository.getSchema(), idList));
    }

    public boolean needPagination() {
        return offset >= 0 && limit > 0;
    }

    public List<JqlEntity> execute() {
        return repository.find(this);
    }

    public long count() {
        return repository.count(filter);
    }

    @Data
    public static class Request {
        private String select;
        private String sort;
        private Integer page;
        private Integer limit;

        @Schema(implementation = Object.class)
        private HashMap filter;

        public Object execute(JqlRepository repository) {
            JqlQuery query = buildQuery(repository);
            List<JqlEntity> res = query.execute();// repository.select(query);
            return wrapResult(res, query);
        }

        public JqlQuery buildQuery(JqlRepository repository) {
            String[] _select = select == null ? null : parsePropertySelection(select);
            Sort _sort = parseSort(sort);
            int _limit = limit == null ? 0 : limit;
            int _page = page == null ? -1 : page;

            JqlQuery query = JqlQuery.of(repository, _select, _sort, _page * _limit, _limit, filter);
            return query;
        }

        protected Object wrapResult(List<JqlEntity> result, JqlQuery query) {
            if (query.needPagination()) {
                long count = query.count();
                PageRequest pageReq = PageRequest.of(page, limit);
                return new PageImpl(result, pageReq, count);
            } else {
                return KVEntity.of("content", result);
            }
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
