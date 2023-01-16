package org.eipgrid.jql.schema;

import org.eipgrid.jql.util.ClassUtils;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

public enum JQType {
    Boolean,
    Integer,
    Float,
    Text,
    Date,
    Time,
    Timestamp,
    Json,
    Array,

    Object;

    public boolean isPrimitive() {
        return this.ordinal() < Array.ordinal();
    }

    public static JQType of(Field f) {
        Class javaType = f.getType();
        if (javaType.isEnum()) {
            Enumerated e = f.getAnnotation(Enumerated.class);
            if (e != null && e.value() == EnumType.STRING) {
                return JQType.Text;
            }
            else {
                return JQType.Integer;
            }
        }
        return JQType.of(javaType);
    }


    public static JQType of(Class javaType) {
        if (javaType.getAnnotation(MappedSuperclass.class) != null
                ||  javaType.getAnnotation(Embeddable.class) != null) {
            return JQType.Object;
        }
        if (javaType == Object.class ||
                Map.class.isAssignableFrom(javaType)) {
            return JQType.Json;
        }
        if (java.util.Collection.class.isAssignableFrom(javaType)) {
            return JQType.Array;
        }
        if (javaType == java.sql.Timestamp.class) {
            return JQType.Timestamp;
        }
        if (javaType == Instant.class || javaType == ZonedDateTime.class) {
            return JQType.Timestamp;
        }

        if (javaType == java.sql.Time.class) {
            return JQType.Time;
        }
        if (javaType == java.sql.Date.class) {
            return JQType.Date;
        }

        javaType = ClassUtils.getBoxedType(javaType);
        if (javaType == Boolean.class || Number.class.isAssignableFrom(javaType)) {
            if (javaType == Float.class || javaType == Double.class) {
                return JQType.Float;
            }
            return JQType.Integer;
        }
        return javaType == String.class ? JQType.Text : JQType.Object;
    }
}
