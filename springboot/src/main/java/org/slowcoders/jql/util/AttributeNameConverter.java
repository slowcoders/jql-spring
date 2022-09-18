package org.slowcoders.jql.util;

//import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import org.apache.commons.text.CaseUtils;

import java.util.ArrayList;
import java.util.List;

public interface AttributeNameConverter {

    String toPhysicalColumnName(String fieldName);

    String toLogicalAttributeName(String columnName);

    AttributeNameConverter bypassConverter = new BypassConverter();
    AttributeNameConverter camelCaseConverter = new CamelCaseConverter();
    AttributeNameConverter defaultConverter = camelCaseConverter;


    class BypassConverter implements AttributeNameConverter {
        @Override
        public String toPhysicalColumnName(String fieldName) {
            return fieldName;
        }

        @Override
        public String toLogicalAttributeName(String columnName) {
            return columnName;
        }

    };


    class CamelCaseConverter implements AttributeNameConverter {


        private final char[] camelCaseDelimiters = {'_'};

        public CamelCaseConverter() {
        }

        @Override
        public String toPhysicalColumnName(String fieldName) {
            String name = fieldName;
            if (name.charAt(0) != '_') {
                name = toSnakeCase(fieldName);
            }
            return name;
        }

        @Override
        public String toLogicalAttributeName(String columnName) {
            String name = columnName;
            if (columnName.charAt(0) != '_') {
                name = CaseUtils.toCamelCase(columnName, false, camelCaseDelimiters);
            }
            return name;
        }

        String toSnakeCase(String name) {
            StringBuilder builder = new StringBuilder(name.replace('.', '_'));

            for (int i = 1; i < builder.length() - 1; ++i) {
                if (this.isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                    builder.insert(i++, '_');
                }
            }
            return builder.toString();
        }


        private boolean isUnderscoreRequired(char before, char current, char after) {
            return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);

        }
    }

    default String[] toPhysicalColumnNames(String[] fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) return null;
        String[] out = new String[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i ++) {
            String key = fieldNames[i];
            out[i] = toPhysicalColumnName(key);
        }
        return out;
    }

    default List<String> toPhysicalColumnNames(Iterable<String> fieldNames) {
        ArrayList<String> out = new ArrayList<>();
        for (String key : fieldNames) {
            out.add(toPhysicalColumnName(key));
        }
        return out;
    }
}
