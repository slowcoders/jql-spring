package org.slowcoders.jql;

import org.slowcoders.jql.util.ClassUtils;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

public enum ValueFormat {
    Boolean,
    Int,
    Float,
    Text,
    Date,
    Time,
    Timestamp,
    Collection,
    Embedded;


    public static ValueFormat resolveValueFormat(Field f) {
        Class javaType = f.getType();
        if (javaType.isEnum()) {
            Enumerated e = f.getAnnotation(Enumerated.class);
            if (e != null && e.value() == EnumType.STRING) {
                return ValueFormat.Text;
            }
            else {
                return ValueFormat.Int;
            }
        }
        return resolveValueFormat(javaType);
    }

    public static ValueFormat resolveValueFormat(Class javaType) {
        if (javaType.getAnnotation(MappedSuperclass.class) != null
                ||  javaType.getAnnotation(Embeddable.class) != null) {
            if (true) {
                throw new RuntimeException("Is it correct??");
            }
            return ValueFormat.Embedded;
        }
        if (javaType == Object.class ||
                Map.class.isAssignableFrom(javaType)) {
            return ValueFormat.Embedded;
        }
        if (java.util.Collection.class.isAssignableFrom(javaType)) {
            return ValueFormat.Collection;
        }
        if (javaType == java.sql.Timestamp.class) {
            return ValueFormat.Timestamp;
        }
        if (javaType == Instant.class || javaType == ZonedDateTime.class) {
            return ValueFormat.Timestamp;
        }

        if (javaType == java.sql.Time.class) {
            return ValueFormat.Time;
        }
        if (javaType == java.sql.Date.class) {
            return ValueFormat.Date;
        }

        javaType = ClassUtils.getBoxedType(javaType);
        if (javaType == Boolean.class || Number.class.isAssignableFrom(javaType)) {
            if (javaType == Float.class || javaType == Double.class) {
                return ValueFormat.Float;
            }
            return ValueFormat.Int;
        }
        return ValueFormat.Text;
    }
}
