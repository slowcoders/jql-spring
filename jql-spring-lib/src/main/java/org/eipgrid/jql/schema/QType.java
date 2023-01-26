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

public enum QType {
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
        return this.ordinal() < Json.ordinal();
    }

    public static QType of(Field f) {
        Class javaType = f.getType();
        if (javaType.isEnum()) {
            Enumerated e = f.getAnnotation(Enumerated.class);
            if (e != null && e.value() == EnumType.STRING) {
                return QType.Text;
            }
            else {
                return QType.Integer;
            }
        }
        return QType.of(javaType);
    }


    public static QType of(Class javaType) {
        if (javaType.getAnnotation(MappedSuperclass.class) != null
                ||  javaType.getAnnotation(Embeddable.class) != null) {
            return QType.Object;
        }
        if (javaType == Object.class ||
                Map.class.isAssignableFrom(javaType)) {
            return QType.Json;
        }
        if (java.util.Collection.class.isAssignableFrom(javaType)) {
            return QType.Array;
        }
        if (javaType == java.sql.Timestamp.class) {
            return QType.Timestamp;
        }
        if (javaType == Instant.class || javaType == ZonedDateTime.class) {
            return QType.Timestamp;
        }

        if (javaType == java.sql.Time.class) {
            return QType.Time;
        }
        if (javaType == java.sql.Date.class) {
            return QType.Date;
        }

        javaType = ClassUtils.getBoxedType(javaType);
        if (javaType == Boolean.class || Number.class.isAssignableFrom(javaType)) {
            if (javaType == Float.class || javaType == Double.class) {
                return QType.Float;
            }
            return QType.Integer;
        }
        return javaType == String.class ? QType.Text : QType.Object;
    }
}