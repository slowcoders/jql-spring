package org.slowcoders.jql.csv;

import org.slowcoders.jql.util.ClassUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class CsvColumn {
    private final ValueType valueType;
    private final Field field;
    private final Class<?> elementType;
    private boolean isNullable;

    public CsvColumn(Field f) {
        this.field = f;
        this.valueType = ValueType.resolveValueFormat(f);
        this.isNullable = f.getAnnotation(Nullable.class) != null;
        if (this.valueType == ValueType.Collection) {
            this.elementType = ClassUtils.getElementType(f);
        } else {
            this.elementType = f.getType();
        }
    }

    public final String getName() {
        return field.getName();
    }

    public Class getElementType() {
        return elementType;
    }

    boolean isNullable() {
        return this.isNullable;
    }

    public ValueType getValueType() {
        return this.valueType;
    }
}