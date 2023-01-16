package org.eipgrid.jql.jpa;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eipgrid.jql.schema.JQColumn;
import org.eipgrid.jql.schema.JQSchema;
import org.eipgrid.jql.schema.JQType;
import org.eipgrid.jql.schema.JQSchemaLoader;
import org.eipgrid.jql.util.AttributeNameConverter;
import org.eipgrid.jql.util.ClassUtils;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class JpaColumn extends JQColumn {

    private boolean isReadOnly;
    private boolean isAutoIncrement;
    private boolean isNullable;
    private boolean isPk;

    private String label;
    private JoinedColumn fk;

    @JsonIgnore
    private Field field;

    public JpaColumn(Field f, JQSchema schema) {
        super(schema, resolveColumnName(schema, f), ClassUtils.getElementType(f), JQType.of(f));
        super.setMappedField(f);
        this.fk = resolveForeignKey(f);

        this.field = f;

        GeneratedValue gv = f.getAnnotation(GeneratedValue.class);
        this.isAutoIncrement = gv != null && gv.strategy() == GenerationType.IDENTITY;
        this.isPk = JPAUtils.isIdField(f);

        this.isNullable = resolveNullable(f);
        this.isReadOnly = gv != null;
        this.label = null;
    }

    public static boolean resolveNullable(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null) return column.nullable();
        for (Annotation a : f.getAnnotations()) {
            if (a.annotationType().getSimpleName().contains("NotNull")) {
                return false;
            }
        }
        return true;
    }


    @Override
    public String getJsonKey() {
        return this.field.getName();
    }

    @Override
    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    @Override
    public boolean isNullable() {
        return this.isNullable;
    }

    @Override
    public boolean isAutoIncrement() {
        return this.isAutoIncrement;
    }

    @Override
    public boolean isPrimaryKey() { return this.isPk; }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public JQColumn getJoinedPrimaryColumn() {
        return fk == null ? null : fk.getJoinedColumn();
    }



    private JoinedColumn resolveForeignKey(Field f) {
        JoinColumn jc = f.getAnnotation(JoinColumn.class);
        if (jc == null) return null;
        return new JoinedColumn(getSchema().getSchemaLoader(), f.getType(), jc.referencedColumnName());
    }


    private static class JoinedColumn {
        private final JQSchemaLoader schemaLoader;
        private final Class entityType;
        private final String columnName;
        private JQColumn pk;

        JoinedColumn(JQSchemaLoader schemaLoader, Class entityType, String columnName) {
            this.schemaLoader = schemaLoader;
            this.entityType = entityType;
            this.columnName = columnName;
        }

        public JQColumn getJoinedColumn() {
            if (pk == null) {
                pk = schemaLoader.loadSchema(entityType).getColumn(columnName);
                assert (pk != null);
            }
            return pk;
        }
    }

    private static String resolveColumnName(JQSchema schema, Field f) {
        if (true) {
            Column c = f.getAnnotation(Column.class);
            if (c != null) {
                String colName = c.name();
                if (colName != null && colName.length() > 0) {
                    return colName;
                }
            }
        }
        if (true) {
            JoinColumn c = f.getAnnotation(JoinColumn.class);
            if (c != null) {
                String colName = c.name();
                if (colName != null && colName.length() > 0) {
                    return colName;
                }
            }
        }
        AttributeNameConverter cvt = schema.getSchemaLoader().getNameConverter();
        String colName = cvt.toPhysicalColumnName(f.getName());
        return colName;
    }
}
