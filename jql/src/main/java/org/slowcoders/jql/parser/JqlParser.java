package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.springframework.core.convert.ConversionService;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.*;

public class JqlParser {

    private final ConversionService conversionService;
    private final JqlQuery where;


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
        // "@except" : {},  "@except" : [ {}, {} ] 추가
        // "select@" : ["attr1", "attr2", "attr3" ] 추가??
        // "select+@" : ["attr1", "attr2", "attr3" ] 추가??
        // "groupBy@" : ["attr1", "attr2/attr3" ]

        Filter baseScope = predicates.getEntityPredicates();
        List<String> selectedAttrs = (List<String>)filter.get(SELECT_MORE);
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            int op_start = key.indexOf('@');
            String function = null;
            if (op_start >= 0) {
                function = key.substring(op_start + 1).toLowerCase().intern();
                key = key.substring(0, op_start);
            }

            PredicateParser op = PredicateParser.getParser(function);
            if (!isValidKey(key)) {
                if (op.isAttributeNameRequired()) {
                    throw new IllegalArgumentException("invalid JQL key: " + entry.getKey());
                }
                key = null;
            }

            Filter.Type valueCategory = this.getValueCategory(value);
            PredicateSet targetPS = key == null ? predicates
                : baseScope.getFilterNode(key, valueCategory, op.needFetchData());

            Predicate cond;
            if (valueCategory == Filter.Type.Entity) {
                this.parse(targetPS, (Map<String, Object>)value);
                cond = targetPS;
            }
            else if (valueCategory == Filter.Type.Entities) {
                for (Map<String, Object> c : (Collection<Map<String, Object>>)value) {
                    this.parse(targetPS, (Map)c);
                }
                cond = targetPS;
            }
            else {
                if (selectedAttrs != null && !selectedAttrs.contains(key)) {
                    selectedAttrs.add(key);
                }

                Filter targetScope = targetPS.getEntityPredicates();
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
            if (cond != predicates) {
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

    private Filter.Type getValueCategory(Object value) {
        if (value instanceof Collection) {
            if (((Collection)value).iterator().next() instanceof Map) {
                return Filter.Type.Entities;
            }
        }
        if (value instanceof Map) {
            return Filter.Type.Entity;
        }
        return Filter.Type.Leaf;
    }

}

