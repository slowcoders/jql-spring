package org.slowcoders.hyperql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.*;

/**
 * 1) select 가 지정되지 않으면, 쿼리 문에 사용된 Entity 들의 Property 들을 검색 결과에 포함시킨다.
 * 2) select 를 이용하여 검색 결과에 포함할 Property 들을 명시할 수 있다.
 *    - Joined Property 명만 명시된 경우, 해당 Joined Entity 의 모든 Property 를 선택.
 */
public interface RestTemplate {

    @Getter
    class Response {

        @Schema(implementation = Object.class)
        private Properties metadata;
        private Object content;

        @JsonIgnore
        private HyperSelect.ResultMap resultMapping;

        @Getter
        @JsonIgnore
        private HyperQuery query;

        private Response(Object content, HyperSelect.ResultMap resultMapping) {
            this.content = content;
            this.resultMapping = resultMapping;
        }

        private Response(Properties metadata) {
            this.metadata = metadata;
        }

        public static Response of(String property, Object value) {
            Properties properties = new Properties();
            properties.put(property, value);
            return new Response(properties);
        }

        public static Response of(Object content, HyperSelect select) {
            Response resp;
            if (isJpaType(content)) {
                resp = new JpaFilter(content, select.getPropertyMap());
            } else {
                resp = new Response(content, select.getPropertyMap());
            }
            return resp;
        }

        private static boolean isJpaType(Object content) {
            if (content == null) return false;
            Class clazz = content.getClass();
            if (List.class.isAssignableFrom(clazz)) {
                List list = (List)content;
                if (list.size() == 0) return false;
                clazz = list.get(0).getClass();
            }
            return clazz.getAnnotation(Entity.class) != null;
        }

        public void setProperty(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new Properties();
            }
            this.metadata.put(key, value);
        }

        @JsonSerialize(using = JpaFilter.Serializer.class)
        public static class JpaFilter extends Response {
            public static final String HQL_RESULT_MAPPING_KEY = "hql-result-mapping";

            /*internal*/ JpaFilter(Object content, HyperSelect.ResultMap resultMappings) {
                super(content, resultMappings);
            }

            static class Serializer extends StdSerializer<JpaFilter> {
                protected Serializer() {
                    super(RestTemplate.Response.JpaFilter.class);
                }

                @Override
                public void serialize(RestTemplate.Response.JpaFilter value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                    provider.setAttribute(HQL_RESULT_MAPPING_KEY, value.getResultMapping());
                    JsonSerializer<Object> s = provider.findValueSerializer(RestTemplate.Response.class);
                    s.serialize(value, gen, provider);
                }
            }
        }
    }


    default Response search(EntitySet entitySet, OutputOptions params, Map<String, Object> filter) throws Exception {
        HyperQuery query = entitySet.createQuery(filter);
        query.select(params.select);
        if (params.sort != null) query.sort(params.sort);
        boolean needPagination = false;
        if (params.limit != null) {
            int limit = params.limit;
            query.limit(limit);
            if (params.page != null) {
                query.offset(params.page * limit);
            }
            needPagination = limit > 0 && query.getOffset() >= 0;
        }
        if (params.distinct != null) query.distinct((boolean) params.distinct);
        if (params.viewParams != null) query.viewParams(params.viewParams);
//        List<Object> result = query.getResultList(params.output);
        Response resp = query.execute(params.output);
        resp.query = query;
        if (needPagination) {
            resp.setProperty("totalElements", query.count());
        }
        return resp;
    }

    static Sort.Order parseOrder(String column) {
        char first_ch = column.charAt(0);
        boolean ascend = first_ch != '-';
        String name = (ascend && first_ch != '+') ? column : column.substring(1);
        return ascend ? Sort.Order.asc(name) : Sort.Order.desc(name);
    }

    static Sort buildSort(String[] orders) {
        if (orders == null || orders.length == 0) {
            return Sort.unsorted();
        }
        ArrayList<Sort.Order> _orders = new ArrayList<>();
        for (String column : orders) {
            Sort.Order order = parseOrder(column);
            _orders.add(order);
        }
        return Sort.by(_orders);
    }

    default EntitySet.InsertPolicy parseInsertPolicy(String onConflict) {
        EntitySet.InsertPolicy insertPolicy;
        if (onConflict == null) {
            insertPolicy = EntitySet.InsertPolicy.ErrorOnConflict;
        } else {
            switch (onConflict.toLowerCase()) {
                case "error":
                    insertPolicy = EntitySet.InsertPolicy.ErrorOnConflict;
                    break;
                case "ignore":
                    insertPolicy = EntitySet.InsertPolicy.IgnoreOnConflict;
                    break;
                case "update":
                    insertPolicy = EntitySet.InsertPolicy.UpdateOnConflict;
                    break;
                default:
                    throw new IllegalArgumentException("unknown onConflict option: " + onConflict);
            }
        }
        return insertPolicy;
    }
}
