package org.eipgrid.jql.parser;

import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSelect;
import org.springframework.core.convert.ConversionService;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.*;

public class JqlParser {

    private final ConversionService conversionService;
    private final JqlQuery where;
    private static String[] emptyColumns = new String[0];

    public JqlParser(JqlSchema schema, ConversionService conversionService) {
        this.where = new JqlQuery(schema);
        this.conversionService = conversionService;
    }

    public JqlQuery parse(Map<String, Object> filter) {
        this.parse(where, filter);
        return where;
    }

    private final static String SELECT_MORE = "select+";
    public void parse(PredicateSet predicates, Map<String, Object> filter) {
        // "joinColumn명" : { "id@?EQ" : "joinedColumn2.joinedColumn3.columnName" }; // Fetch 자동 수행.
        //   --> @?EQ 기능은 넣되, 숨겨진 고급기능으로..
        // "groupBy@" : ["attr1", "attr2/attr3" ]

        Filter baseFilter = predicates.getBaseFilter();
        List<String> selectedAttrs = (List<String>)filter.get(SELECT_MORE);
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            int op_start = key.indexOf('@');
            String function = null;
            if (op_start >= 0) {
                function = key.substring(op_start + 1).toLowerCase().trim();
                key = key.substring(0, op_start).trim();
            }

            PredicateFactory op = PredicateFactory.getFactory(function);
            boolean fetchData = op.needFetchData();
            JqlSelect select = JqlSelect.All;
            if (key.length() > 0 && key.charAt(key.length() - 1) == '>') {
                int p = key.lastIndexOf('<');
                String keys = key.substring(p + 1, key.length()-1);
                if (keys.length() > 0) {
                    select = JqlSelect.by(keys, null, 0, 0);
                }
                key = key.substring(0, p).trim();
            }
            if (!isValidKey(key)) {
                if (op.isAttributeNameRequired()) {
                    throw new IllegalArgumentException("invalid JQL key: " + entry.getKey());
                }
                key = null;
            }

            ValueNodeType valueCategory = this.getValueCategory(value);
            Filter targetNode = baseFilter.getFilterNode(key, valueCategory);
            if (targetNode != baseFilter) {
                if (!fetchData) {
                    targetNode.setSelectedColumns(JqlSelect.NotAtAll);
                } else {
                    targetNode.setSelectedColumns(select);
                }
            }

            Predicate cond;
            if (valueCategory == ValueNodeType.Entity) {
                PredicateSet ps = op.getPredicateNode(targetNode, valueCategory);
                this.parse(ps, (Map<String, Object>)value);
                cond = targetNode;
            }
            else if (valueCategory == ValueNodeType.Entities) {
                PredicateSet ps = op.getPredicateNode(targetNode, valueCategory);
                for (Map<String, Object> c : (Collection<Map<String, Object>>)value) {
                    this.parse(ps, (Map)c);
                }
                cond = targetNode;
            }
            else {
                if (selectedAttrs != null && !selectedAttrs.contains(key)) {
                    selectedAttrs.add(key);
                }

                Filter targetScope = targetNode.getBaseFilter();
                String columnName = targetScope.getColumnName(key);
                QAttribute column;
                if (value == null) {
                    column = new QAttribute(targetScope, columnName, null);
                }
                else {
                    if (targetScope.asJsonFilter() == null) {
                        Class<?> fieldType = targetScope.getTableFilter().getSchema().getColumn(columnName).getJavaType();
                        Class<?> accessType = op.getAccessType(value, fieldType);
                        value = conversionService.convert(value, accessType);
                    }
                    column = new QAttribute(targetScope, columnName, value.getClass());
                }
                cond = op.createPredicate(column, value);
            }

            if (cond == null) {
                throw new IllegalArgumentException("invalid value type for " + entry.getKey() + " value: " + value);
            }
            if (cond != predicates && !cond.isEmpty()) {
                predicates.add(cond);
            }
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

    private ValueNodeType getValueCategory(Object value) {
        if (value instanceof Collection) {
            if (((Collection)value).iterator().next() instanceof Map) {
                return ValueNodeType.Entities;
            }
        }
        if (value instanceof Map) {
            return ValueNodeType.Entity;
        }
        return ValueNodeType.Leaf;
    }

}

