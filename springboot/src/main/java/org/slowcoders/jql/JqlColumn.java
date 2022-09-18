package org.slowcoders.jql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.jdbc.timescale.Aggregate;

import java.io.PrintStream;

public abstract class JqlColumn {
    protected Class<?> javaType;
    protected String columnName;
    protected String fieldName;
    protected String label;

    protected boolean isAutoIncrement;
    protected boolean isReadOnly;
    protected boolean isWritable;
    protected boolean isNullable;

    @JsonIgnore
    protected JqlColumnJoin fk;
    protected boolean isPk;
    protected ValueFormat valueFormat;

    @JsonIgnore
    private final JqlSchema schema;
    protected Aggregate.Type aggregationType;

    protected JqlColumn(JqlSchema schema) {
        this.schema = schema;
    }

    public final JqlSchema getSchema() {
        return schema;
    }

    public void dumpORM(PrintStream out) {
        if (getLabel() != null) {
            out.print("\t/** ");
            out.print(getLabel());
            out.println(" */");
        }
        if (isAutoIncrement()) {
            out.println("\t@Id");
            out.println("\t@GeneratedValue(strategy = GenerationType.IDENTITY)");
        }
        if (!isNullable()) {
            out.println("\t@NotNull");
            out.println("\t@Column(nullable = false)");
        }
        if (getPrecision() > 0) {
            out.println("\t@Max(" + getPrecision() +")");
        }

        out.print("\t@Getter");
        if (!isReadOnly() && !isAutoIncrement()) {
            out.print(" @Setter");
        }
        out.println();

        out.println("\t" + getJavaType().getName() + " " + this.getFieldName() + ";");
    }


    public Class<?> getJavaType() {
        return javaType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

//    public boolean isWritable() {
//        return isWritable;
//    }

    public boolean isNullable() {
        return isNullable;
    }

    public abstract int getPrecision();

    public abstract int getScale();

    public abstract String getDBColumnType();

    public JqlColumnJoin getJoinedForeignKey() {
        return fk;
    }

//    public JqlIndex getIndex() {
//        return index;
//    }
//
//    public boolean isPrimaryKey() {
//        return isPk;
//    }
//
    public ValueFormat getValueFormat() {
        return valueFormat;
    }

//    public Field getField() {
//        return field;
//    }

    public Aggregate.Type getAggregationType() {
        return this.aggregationType;
    }



    public Object convertValueToInsert(Object v) {
        if (v == null) return null;


        if (v.getClass().isEnum()) {
            if (this.getValueFormat() == ValueFormat.Text) {
                return v.toString();
            }
            else {
                return ((Enum)v).ordinal();
            }
        }
        return v;
    }

    public JqlColumn getJoinedPrimaryColumn() {
        return null;
    }
}
