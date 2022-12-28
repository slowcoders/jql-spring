package org.eipgrid.jql.csv;

import org.eipgrid.jql.JqlValueKind;
import org.eipgrid.jql.jpa.JpaColumn;
import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;

public class CsvColumn {
    private final JqlValueKind valueType;
    private final Field field;
    private final Class<?> elementType;
    private boolean isNullable;

    public CsvColumn(Field f) {
        this.field = f;
        this.valueType = JqlValueKind.of(f);
        if (this.valueType == JqlValueKind.Array) {
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

    public JqlValueKind getValueType() {
        return this.valueType;
    }
}
