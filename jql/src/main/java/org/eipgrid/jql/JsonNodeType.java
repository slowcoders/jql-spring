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

public enum JsonNodeType {
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

    public static JsonNodeType getNodeType(Field f) {
        Class javaType = f.getType();
        if (javaType.isEnum()) {
            Enumerated e = f.getAnnotation(Enumerated.class);
            if (e != null && e.value() == EnumType.STRING) {
                return JsonNodeType.Text;
            }
            else {
                return JsonNodeType.Integer;
            }
        }
        return JsonNodeType.getNodeType(javaType);
    }


    public static JsonNodeType getNodeType(Class javaType) {
        if (javaType.getAnnotation(MappedSuperclass.class) != null
                ||  javaType.getAnnotation(Embeddable.class) != null) {
            if (true) {
                throw new RuntimeException("Is it correct??");
            }
            return JsonNodeType.Object;
        }
        if (javaType == Object.class ||
                Map.class.isAssignableFrom(javaType)) {
            return JsonNodeType.Object;
        }
        if (java.util.Collection.class.isAssignableFrom(javaType)) {
            return JsonNodeType.Array;
        }
        if (javaType == java.sql.Timestamp.class) {
            return JsonNodeType.Timestamp;
        }
        if (javaType == Instant.class || javaType == ZonedDateTime.class) {
            return JsonNodeType.Timestamp;
        }

        if (javaType == java.sql.Time.class) {
            return JsonNodeType.Time;
        }
        if (javaType == java.sql.Date.class) {
            return JsonNodeType.Date;
        }

        javaType = ClassUtils.getBoxedType(javaType);
        if (javaType == Boolean.class || Number.class.isAssignableFrom(javaType)) {
            if (javaType == Float.class || javaType == Double.class) {
                return JsonNodeType.Float;
            }
            return JsonNodeType.Integer;
        }
        return JsonNodeType.Text;
    }
}
