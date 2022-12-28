package org.eipgrid.jql;

import org.eipgrid.jql.util.ClassUtils;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

public enum JqlValueKind {
    Boolean,
    Integer,
    Float,
    Text,
    Date,
    Time,
    Timestamp,
    Array,
    Object;

    public boolean isPrimitive() {
        return this.ordinal() < Array.ordinal();
    }

    public static JqlValueKind of(Field f) {
        Class javaType = f.getType();
        if (javaType.isEnum()) {
            Enumerated e = f.getAnnotation(Enumerated.class);
            if (e != null && e.value() == EnumType.STRING) {
                return JqlValueKind.Text;
            }
            else {
                return JqlValueKind.Integer;
            }
        }
        return JqlValueKind.of(javaType);
    }


    public static JqlValueKind of(Class javaType) {
        if (javaType.getAnnotation(MappedSuperclass.class) != null
                ||  javaType.getAnnotation(Embeddable.class) != null) {
            return JqlValueKind.Object;
        }
        if (javaType == Object.class ||
                Map.class.isAssignableFrom(javaType)) {
            return JqlValueKind.Object;
        }
        if (java.util.Collection.class.isAssignableFrom(javaType)) {
            return JqlValueKind.Array;
        }
        if (javaType == java.sql.Timestamp.class) {
            return JqlValueKind.Timestamp;
        }
        if (javaType == Instant.class || javaType == ZonedDateTime.class) {
            return JqlValueKind.Timestamp;
        }

        if (javaType == java.sql.Time.class) {
            return JqlValueKind.Time;
        }
        if (javaType == java.sql.Date.class) {
            return JqlValueKind.Date;
        }

        javaType = ClassUtils.getBoxedType(javaType);
        if (javaType == Boolean.class || Number.class.isAssignableFrom(javaType)) {
            if (javaType == Float.class || javaType == Double.class) {
                return JqlValueKind.Float;
            }
            return JqlValueKind.Integer;
        }
        return JqlValueKind.Text;
    }
}
