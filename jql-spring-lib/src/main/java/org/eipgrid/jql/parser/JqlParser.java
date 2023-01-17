package org.eipgrid.jql.parser;

import org.eipgrid.jql.schema.JQColumn;
import org.eipgrid.jql.schema.JQSchema;
import org.eipgrid.jql.JqlSelect;
import org.springframework.core.convert.ConversionService;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.*;

public class JqlParser {

    private final ConversionService conversionService;

    public JqlParser(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public JqlQuery parse(JQSchema schema, Map<String, Object> jsQuery, JqlSelect select) {
        JqlQuery where = new JqlQuery(schema);
        if (select != null) {
            where.setSelectedProperties(select.getPropertyKeys());
        }
        if (jsQuery != null) {
            this.parse(where.getPredicateSet(), jsQuery, true);
        }
        return where;
    }

    public void parse(PredicateSet predicates, Map<String, Object> filter, boolean excludeConstantAttribute) {
        // "joinColumn명" : { "id@?EQ" : "joinedColumn2.joinedColumn3.columnName" }; // Fetch 자동 수행.
        //   --> @?EQ 기능은 넣되, 숨겨진 고급기능으로..
        // "groupBy@" : ["attr1", "attr2/attr3" ]

        EntityFilter baseFilter = predicates.getBaseFilter();

        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            int select_end = key.lastIndexOf('>');
            int op_start = key.lastIndexOf('@');
            String function = null;
            if (op_start > select_end) {
                function = key.substring(op_start + 1).toLowerCase().trim();
                key = key.substring(0, op_start).trim();
            }

            PredicateFactory op = PredicateFactory.getFactory(function);
            String[] selectedKeys = null;
            if (select_end > 0) {
                int select_start = key.indexOf('<');
                if (select_start > 0 && select_start < select_end) {
                    String keys = key.substring(select_start+1, select_end);
                    selectedKeys = JqlSelect.splitPropertyKeys(keys);
                    key = key.substring(0, select_start);
                }
            }

            /** [has not 구현]
                SELECT
                    t_0.*, pt_1.episode_id --, t_1.published
                FROM starwars.character as t_0
                    left join starwars.character_episode_link as pt_1 on
                    t_0.id = pt_1.character_id and pt_1.episode_id = 'JEDI'
                where pt_1.character_id is null;
                ---------------------------------
                while (key.startsWith("../")) {
                    baseFilter = baseFilter.getParentNode();
                    key = key.substring(3);
                }
             */

            if (!baseFilter.isJsonNode() && !isValidKey(key)) {
                if (op.isAttributeNameRequired()) {
                    if (op_start == 0) {
                        throw new IllegalArgumentException("Property name is missing : " + entry.getKey());
                    }
                    throw new IllegalArgumentException("invalid JQL key: " + entry.getKey());
                }
                key = null;
            }

            NodeType valueNodeType = this.getNodeType(value);
            EntityFilter subFilter = baseFilter.getFilterNode(key, valueNodeType);
            PredicateSet targetPredicates = predicates;
            if (subFilter != baseFilter) {
                targetPredicates = subFilter.getPredicateSet();
            }

            if (valueNodeType != NodeType.Leaf) {
                subFilter.setSelectedProperties(selectedKeys);
                PredicateSet ps = op.getPredicates(subFilter, valueNodeType);
                if (valueNodeType == NodeType.Entity) {
                    Map<String, Object> subJql = (Map<String, Object>) value;
                    if (!subJql.isEmpty()) {
                        this.parse(ps, subJql, true);
                    }
                    else if (selectedKeys == null) {
                        subFilter.setSelectedProperties_withEmptyFilter();
                    }
                }
                else {  // ValueNodeType.Entities
                    for (Map<String, Object> map : (Collection<Map<String, Object>>) value) {
                        PredicateSet and_qs = new PredicateSet(Conjunction.AND, ps.getBaseFilter());
                        this.parse(and_qs, map, false);
                        ps.add(and_qs);
                    }
                }
                continue;
            }

            String columnName = subFilter.getColumnName(key);
            if (!excludeConstantAttribute || op_start > 0) {
                subFilter.addComparedPropertyToSelection(columnName);
            }
            if (value != null) {
                JQSchema schema = subFilter.getSchema();
                if (schema != null) {
                    JQColumn column = schema.getColumn(columnName);
                    Class<?> fieldType = column.getJavaType();
                    Class<?> accessType = op.getAccessType(value, fieldType);
                    value = conversionService.convert(value, accessType);
                }
            }
            Expression cond = op.createPredicate(columnName, value);
            targetPredicates.add(cond);
        }
    }

    private boolean isValidKey(String key) {
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

    private static HashMap<Class, String[]> autoFetchFields = new HashMap<>();
    private String[] getFetchEagerFields(Class<?> entityType) {
        synchronized (autoFetchFields) {
            String[] fields = autoFetchFields.get(entityType);
            if (fields == null) {
                ArrayList<String> fieldList = new ArrayList<>();
                registerAutoFetchFields(fieldList, entityType);
                fields = fieldList.toArray(new String[fieldList.size()]);
                autoFetchFields.put(entityType, fields);
            }
            return fields;
        }
    }

    private void registerAutoFetchFields(ArrayList<String> fields, Class<?> entityType) {
        if (entityType == Object.class) {
            return;
        }

        for (Field f : entityType.getDeclaredFields()) {
            ManyToOne mto1 = f.getAnnotation(ManyToOne.class);
            OneToOne oto1 = f.getAnnotation(OneToOne.class);
            if (mto1 != null && mto1.fetch() == FetchType.EAGER ||
                    oto1 != null && oto1.fetch() == FetchType.EAGER) {
                fields.add(f.getName());
            }
        }
        registerAutoFetchFields(fields, entityType.getSuperclass());
    }

    private NodeType getNodeType(Object value) {
        if (value instanceof Collection) {
            if (((Collection)value).iterator().next() instanceof Map) {
                return NodeType.Entities;
            }
        }
        if (value instanceof Map) {
            return NodeType.Entity;
        }
        return NodeType.Leaf;
    }

    enum NodeType {
        Leaf,
        Entity,
        Entities
    }
}

