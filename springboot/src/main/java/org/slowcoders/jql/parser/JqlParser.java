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
    private static final int VT_LEAF = 0;
    private static final int VT_Entity = 1;
    private static final int VT_Entities = 2;


    public JqlParser(JqlSchema schema, ConversionService conversionService) {
        this.where = new JqlQuery(schema);
        this.conversionService = conversionService;
    }

    public JqlQuery parse(Map<String, Object> filter) {
        this.parse(where, filter);
        return where;
    }

    private final static String SELECT_MORE = "select+";
    public void parse(Filter baseNode, Map<String, Object> filter) {
        // "joinColumn명" : { "id@?EQ" : "joinedColumn2.joinedColumn3.columnName" }; // Fetch 자동 수행.
        //   --> @?EQ 기능은 넣되, 숨겨진 고급기능으로..
        // "@except" : {},  "@except" : [ {}, {} ] 추가
        // "select@" : ["attr1", "attr2", "attr3" ] 추가??
        // "select+@" : ["attr1", "attr2", "attr3" ] 추가??
        // "groupBy@" : ["attr1", "attr2/attr3" ]

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

            int valueCategory = this.getValueCategory(value);
            Filter targetNode = key == null ? baseNode
                : baseNode.getContainingFilter(where, key, valueCategory == VT_LEAF);
            if (targetNode.getTable() != baseNode.getTable()) {
                targetNode.getTable().setFetchData(op.needFetchData(), where);
            }

            Predicate cond;
            if (valueCategory == VT_Entity) {
                cond = op.parse(this, targetNode, (Map<String, Object>)value);
            }
            else if (valueCategory == VT_Entities) {
                cond = op.parse(this, targetNode, (Collection<Map<String, Object>>)value);
            }
            else {
                if (selectedAttrs != null && !selectedAttrs.contains(key)) {
                    selectedAttrs.add(key);
                }

                String columnName = targetNode.getColumnName(key);
                if (targetNode.asJsonFilter() == null) {
                    Class<?> fieldType = targetNode.getTable().getSchema().getColumn(columnName).getJavaType();
                    Class<?> accessType = op.getAccessType(value, fieldType);
                    value = conversionService.convert(value, accessType);
                }
                QAttribute column = new QAttribute(targetNode, columnName, value.getClass());
                cond = op.createPredicate(column, value);
            }

            if (cond == null) {
                throw new IllegalArgumentException("invalid value type for " + entry.getKey() + " value: " + value);
            }
            if (cond != targetNode) {
                targetNode.add(cond);
            }
        }
    }

    private boolean isValidKey(String key) {
        int key_length = key.length();
        if (key_length == 0) return false;
        char ch = key.charAt(0);

        if (!Character.isJavaIdentifierStart(ch)) {//.isAlphabetic(ch)) {
            return false;
        }
        for (int i = key.length(); --i > 0; ) {
            ch = key.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
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

    private int getValueCategory(Object value) {
        if (value instanceof Collection) {
            if (((Collection)value).iterator().next() instanceof Map) {
                return VT_Entities;
            }
        }
        if (value instanceof Map) {
            return VT_Entity;
        }
        return VT_LEAF;
    }

}

