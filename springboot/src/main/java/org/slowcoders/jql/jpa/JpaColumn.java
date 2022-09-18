package org.slowcoders.jql.jpa;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlColumnJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.ValueFormat;
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

        this.isReadOnly = false;
        this.isWritable = true;
        this.label = null;
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

    public int getPrecision() {
        Max max = field.getAnnotation(Max.class);
        return max == null ? 0 : (int)max.value();
    }

    @Override
    public int getScale() {
        return -1;
    }

    @Override
    public String getDBColumnType() {
        return getSchema().getSchemaLoader().toColumnType(getJavaType(), field);
    }
}
