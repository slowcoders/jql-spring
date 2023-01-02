package org.eipgrid.jql.csv;

import org.eipgrid.jql.JQType;
import org.eipgrid.jql.jpa.JpaColumn;
import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;

public class CsvColumn {
    private final JQType valueType;
    private final Field field;
    private final Class<?> elementType;
    private boolean isNullable;

    public CsvColumn(Field f) {
        this.field = f;
        this.valueType = JQType.of(f);
        if (this.valueType == JQType.Array) {
            this.elementType = ClassUtils.getElementType(f);
            this.isNullable = false;
        } else {
            this.elementType = f.getType();
            this.isNullable = JpaColumn.resolveNullable(f);
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

    public JQType getValueType() {
        return this.valueType;
    }
}
