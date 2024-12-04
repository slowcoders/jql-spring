package org.slowcoders.hyperql.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.qb.Filter;
import org.slowcoders.hyperql.qb.FilterSet;
import org.slowcoders.hyperql.qb.JoinedFilter;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class HqlParser {

    private final ObjectMapper om;

    public HqlParser(ObjectMapper om) {
        this.om = om;
    }

    public HyperFilter parse(QSchema schema, Map<String, Object> filter) {
        HyperFilter where = new HyperFilter(schema);
        if (filter != null) {
            this.parse(where.getPredicateSet(), filter, true);
        }
        return where;
    }

    public HyperFilter parse(QSchema schema, Filter filter) {
        HyperFilter where = new HyperFilter(schema);
        if (filter != null) {
            this.parse2(where.getPredicateSet(), filter, true);
        }
        return where;
    }

    public void parse2(PredicateSet predicates, Filter filter, boolean excludeConstantAttribute) {
        EntityFilter baseFilter = predicates.getBaseFilter();

        for (Filter entry : filter.entries()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String function = entry.getOperator();
            PredicateFactory op = function == null ? null : PredicateFactory.getFactory(function.toLowerCase());

            if (!baseFilter.isJsonNode() && !isValidKey(key)) {
                if (op.isAttributeNameRequired()) {
                    throw new IllegalArgumentException("invalid HQL key: " + entry.getKey());
                }
                key = null;
            }


            NodeType valueNodeType;
            if (entry instanceof JoinedFilter || entry instanceof FilterSet) {
                valueNodeType = NodeType.Entity;
                op = PredicateFactory.IS;
            } else {
                valueNodeType = this.getNodeType(value);
            }
            EntityFilter subFilter = baseFilter.getFilterNode(key, valueNodeType);
            PredicateSet targetPredicates = predicates;
            if (subFilter != baseFilter) {
                targetPredicates = subFilter.getPredicateSet();
            }

            if (valueNodeType != NodeType.Leaf) {
                PredicateSet ps;
                if ("OR".equals(function)) {
                    ps = new PredicateSet(Conjunction.OR, targetPredicates.getBaseFilter());
                    targetPredicates.add(ps);
                } else {
                    ps = targetPredicates;
                }
                this.parse2(ps, entry, true);
                continue;
            }

            String columnName = subFilter.getColumnName(key);
            QColumn column;
            QSchema schema = subFilter.getSchema();
            if (schema != null) {
                column = schema.getColumn(columnName);
                if (value != null && !column.isJsonNode()) {
                    Class<?> fieldType = column.getValueType();
                    Field f = column.getMappedOrmField();
                    Class<?> accessType = op.getAccessType(value, fieldType);
                    if (f != null && f.getType().isEnum() && Number.class.isAssignableFrom(accessType)) {
                        // 1차 변경.
                        value = om.convertValue(value, f.getType());
                        value = ((Enum)value).ordinal();
                    }
                    if (accessType != java.sql.Date.class || value.getClass() != String.class) {
//                    Class<?> fieldType = column.getValueType();
                        value = om.convertValue(value, accessType);
                    }
                } else {
                    // JsonB column 자체를 문자열로 비교하는 경우에는 별도 conversion 을 실행하지 않는다.
                }
            }
            else {
                column = new JsonColumn(subFilter, columnName, value == null ? String.class : value.getClass());
            }
            Expression cond = op.createPredicate(column, value);
            targetPredicates.add(cond);
        }
    }

    public void parse(PredicateSet predicates, Map<String, Object> filter, boolean excludeConstantAttribute) {
        EntityFilter baseFilter = predicates.getBaseFilter();

        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey().trim();
            Object value = entry.getValue();
            PredicateFactory op = null;
            int op_start;

            if (key.toLowerCase().startsWith("or#")) {
                op = PredicateFactory.OR;
                key = null;
            } else if (key.toLowerCase().startsWith("not#")) {
                op = PredicateFactory.NOT;
                key = null;
            } else {
                op_start = key.lastIndexOf(' ');

                if (op_start >= 0) {
                    String operator = key.substring(op_start + 1).trim();
                    key = key.substring(0, op_start).trim();
                    op = PredicateFactory.getFactory(operator.toLowerCase());
                    if (op == null) {
                        throw new IllegalArgumentException("invalid HQL operator: " + operator);
                    }
                } else {
                    op = PredicateFactory.getUnaryFactory(key.toLowerCase());
                    if (op == null) {
                        op = PredicateFactory.IS;
                    } else {
                        key = null;
                    }
                }
            }

//            if (!baseFilter.isJsonNode() && !isValidKey(key)) {
//                if (op.isAttributeNameRequired()) {
//                    if (op_start == 0) {
//                        throw new IllegalArgumentException("Property name is missing : " + entry.getKey());
//                    }
//                    throw new IllegalArgumentException("invalid HQL key: " + entry.getKey());
//                }
//                key = null;
//            }

            NodeType valueNodeType = this.getNodeType(value);
            if (op == PredicateFactory.OR && valueNodeType != NodeType.Entities) {
                throw new IllegalArgumentException("Or operator must contains entity array");
            }
            EntityFilter subFilter = baseFilter;
            PredicateSet targetPredicates = predicates;
            if (key != null) {
                subFilter = baseFilter.getFilterNode(key, valueNodeType);
                if (subFilter != baseFilter) {
                    targetPredicates = subFilter.getPredicateSet();
                }
            }

            if (valueNodeType != NodeType.Leaf) {
                PredicateSet ps = op.getPredicates(targetPredicates, valueNodeType);
                if (valueNodeType == NodeType.Entity) {
                    Map<String, Object> subJql = (Map<String, Object>) value;
                    if (!subJql.isEmpty()) {
                        this.parse(ps, subJql, true);
                    }
                }
                else {  // ValueNodeType.Entities
                    for (Map<String, Object> map : (Collection<Map<String, Object>>) value) {
                        if (ps.getConjunction() == Conjunction.OR) {
                            PredicateSet and_qs = new PredicateSet(Conjunction.AND, ps.getBaseFilter());
                            this.parse(and_qs, map, false);
                            ps.add(and_qs);
                        } else {
                            this.parse(ps, map, false);
                        }
                    }
                }
                continue;
            }

            String columnName = subFilter.getColumnName(key);
            QColumn column;
            QSchema schema = subFilter.getSchema();
            if (schema != null) {
                column = schema.getColumn(columnName);
                if (value != null && !column.isJsonNode()) {
                    Class<?> fieldType = column.getValueType();
                    Field f = column.getMappedOrmField();
                    Class<?> accessType = op.getAccessType(value, fieldType);
                    if (f != null && f.getType().isEnum() && Number.class.isAssignableFrom(accessType)) {
                        // 1차 변경.
                        value = om.convertValue(value, f.getType());
                        value = ((Enum)value).ordinal();
                    }
                    if (accessType != java.sql.Date.class || value.getClass() != String.class) {
//                    Class<?> fieldType = column.getValueType();
                        value = om.convertValue(value, accessType);
                    }
                } else {
                    // JsonB column 자체를 문자열로 비교하는 경우에는 별도 conversion 을 실행하지 않는다.
                }
            }
            else {
                column = new JsonColumn(subFilter, columnName, value == null ? String.class : value.getClass());
            }
            Expression cond = op.createPredicate(column, value);
            targetPredicates.add(cond);
        }
    }

    private boolean isValidKey(String key) {
        if (key == null) return false;

        int key_length = key.length();
        if (key_length == 0) return false;
        char ch = key.charAt(0);

        if (!Character.isJavaIdentifierStart(ch) && ch != '+') {
            return false;
        }
        for (int i = key.length(); --i > 0; ) {
            ch = key.charAt(i);
            if (ch != '.' && !Character.isJavaIdentifierPart(ch)) {
                return false;
            }
        }
        return true;
    }

    private NodeType getNodeType(Object value) {
        if (value instanceof Collection) {
            Collection values = (Collection) value;
            if (!values.isEmpty()) {
                var firstElement = values.iterator().next();
                if (firstElement instanceof Map) {
                    return NodeType.Entities;
                }
            }
        }
        if (value instanceof Map) {
            return NodeType.Entity;
        }
        return NodeType.Leaf;
    }


    public enum NodeType {
        Leaf,
        Entity,
        Entities,
    }
}

