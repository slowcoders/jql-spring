package org.slowcoders.jql.csv;

import org.slowcoders.jql.util.ClassUtils;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

public enum ValueType {
    Boolean,
    Int,
    Float,
    Text,
    Date,
    Time,
    Timestamp,
    Collection,
    Embedded;

    public static ValueType resolveValueFormat(Field f) {
        Class javaType = f.getType();
        if (javaType.isEnum()) {
            Enumerated e = f.getAnnotation(Enumerated.class);
            if (e != null && e.value() == EnumType.STRING) {
                return ValueType.Text;
            }
            else {
                return ValueType.Int;
            }
        }
        return resolveValueFormat(javaType);
    }

    public static ValueType resolveValueFormat(Class javaType) {
        if (javaType.getAnnotation(MappedSuperclass.class) != null
                ||  javaType.getAnnotation(Embeddable.class) != null) {
            return ValueType.Embedded;
        }
        if (javaType == Object.class ||
                Map.class.isAssignableFrom(javaType)) {
            return ValueType.Embedded;
        }
        if (java.util.Collection.class.isAssignableFrom(javaType)) {
            return ValueType.Collection;
        }
        if (javaType == java.sql.Timestamp.class) {
            return ValueType.Timestamp;
        }
        if (javaType == Instant.class || javaType == ZonedDateTime.class) {
            return ValueType.Timestamp;
        }

        if (javaType == java.sql.Time.class) {
            return ValueType.Time;
        }
        if (javaType == java.sql.Date.class) {
            return ValueType.Date;
        }

        javaType = ClassUtils.getBoxedType(javaType);
        if (javaType == Boolean.class || Number.class.isAssignableFrom(javaType)) {
            if (javaType == Float.class || javaType == Double.class) {
                return ValueType.Float;
            }
            return ValueType.Int;
        }
        return ValueType.Text;
    }
}
