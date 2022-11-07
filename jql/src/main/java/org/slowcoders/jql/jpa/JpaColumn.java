package org.slowcoders.jql.jpa;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.*;
import org.slowcoders.jql.jdbc.timescale.Aggregate;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.slowcoders.jql.util.ClassUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.lang.reflect.Field;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class JpaColumn extends JqlColumn {

    private boolean isReadOnly;
    private boolean isAutoIncrement;
    private boolean isNullable;
    private boolean isPk;

    private String label;
    private JoinedColumn fk;

    @JsonIgnore
    private Field field;

    public JpaColumn(Field f, JqlSchema schema) {
        super(schema, resolveColumnName(schema, f), ClassUtils.getElementType(f), JsonNodeType.getNodeType(f));
        this.fk = resolveForeignKey(f);

        this.field = f;

        GeneratedValue gv = f.getAnnotation(GeneratedValue.class);
        this.isAutoIncrement = gv != null && gv.strategy() == GenerationType.IDENTITY;
        this.isPk = JPAUtils.isIdField(f);
        this.isNullable = f.getAnnotation(Nullable.class) != null;

        this.isReadOnly = gv != null;
        this.label = null;
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
    public JqlColumn getJoinedPrimaryColumn() {
        return fk == null ? null : fk.getJoinedColumn();
    }

    @Override
    public String getDBColumnType() {
        return getSchema().getSchemaLoader().toColumnType(getJavaType(), field);
    }



    private JoinedColumn resolveForeignKey(Field f) {
        JoinColumn jc = f.getAnnotation(JoinColumn.class);
        if (jc == null) return null;
        return new JoinedColumn(getSchema().getSchemaLoader(), f.getType(), jc.referencedColumnName());
    }


    private static class JoinedColumn {
        private final SchemaLoader schemaLoader;
        private final Class entityType;
        private final String columnName;
        private JqlColumn pk;

        JoinedColumn(SchemaLoader schemaLoader, Class entityType, String columnName) {
            this.schemaLoader = schemaLoader;
            this.entityType = entityType;
            this.columnName = columnName;
        }

        public JqlColumn getJoinedColumn() {
            if (pk == null) {
                pk = schemaLoader.loadSchema(entityType).getColumn(columnName);
                assert (pk != null);
            }
            return pk;
        }
    }

    private static String resolveColumnName(JqlSchema schema, Field f) {
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
