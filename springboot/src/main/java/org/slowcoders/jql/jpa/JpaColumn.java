package org.slowcoders.jql.jpa;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.*;
import org.slowcoders.jql.jdbc.timescale.Aggregate;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.slowcoders.jql.util.ClassUtils;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.validation.constraints.Max;
import java.lang.reflect.Field;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class JpaColumn extends JqlColumn {
    protected boolean isReadOnly;
    protected boolean isAutoIncrement;
    protected boolean isNullable;
    protected boolean isPk;

    private String label;
    protected JqlColumnJoin fk;

    @JsonIgnore
    protected Field field;
    public JpaColumn(Field f, JqlSchema schema) {
        super(schema);
        this.columnName = resolveColumnName(f);
        this.fieldName = f.getName();
        this.fk = resolveForeignKey(f);

        this.valueFormat = ValueFormat.resolveValueFormat(f);
        if (this.getValueFormat() == ValueFormat.Collection) {
            this.javaType = ClassUtils.getElementType(f);
        }
        else {
            this.javaType = f.getType();
        }
        this.field = f;

        GeneratedValue gv = f.getAnnotation(GeneratedValue.class);
        this.isAutoIncrement = gv != null && gv.strategy() == GenerationType.IDENTITY;
        this.isPk = JPAUtils.isIdField(f);
        this.isNullable = f.getAnnotation(Nullable.class) != null;

        this.aggregationType = resolveAggregationType(f);

        this.isReadOnly = gv != null;
        this.label = null;
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
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getDBColumnType() {
        return getSchema().getSchemaLoader().toColumnType(getJavaType(), field);
    }



    private JqlColumnJoin resolveForeignKey(Field f) {
        JoinColumn jc = f.getAnnotation(JoinColumn.class);
        if (jc == null) return null;
        String pkTable = getSchema().getSchemaLoader().resolveTableName(f.getType());
        String pkColumn = jc.referencedColumnName();
        String fkColumn = resolveColumnName(f);

        return new JqlColumnJoin(pkTable, pkColumn, getSchema().getTableName(), fkColumn, getSchema().getSchemaLoader());
    }

    private String resolveColumnName(Field f) {
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
        AttributeNameConverter cvt = getSchema().getSchemaLoader().getNameConverter();
        String colName = cvt.toPhysicalColumnName(f.getName());
        return colName;
    }

    private Aggregate.Type resolveAggregationType(Field f) {
        Aggregate c = f.getAnnotation(Aggregate.class);
        if (c != null) {
            return c.value();
        }
        if (this.valueFormat == ValueFormat.Float) {
            return Aggregate.Type.Mean;
        }
        return Aggregate.Type.None;
    }

    public JqlColumnJoin getJoinedForeignKey() {
        return fk;
    }
}
