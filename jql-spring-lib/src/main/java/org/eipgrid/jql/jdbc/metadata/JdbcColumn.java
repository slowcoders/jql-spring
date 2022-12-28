package org.eipgrid.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlEntityJoin;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlValueKind;
import org.eipgrid.jql.util.ClassUtils;

import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class JdbcColumn extends JqlColumn {

    private final boolean isReadOnly;
    private final boolean isAutoIncrement;
    private final boolean isNullable;
    private final boolean isPk;

    private String fieldName;
    private final String colTypeName;

    private String label;
    private ColumnBinder pkBinder;

    private int displaySize;
//    @JsonIgnore
//    private JqlIndex index;

    @JsonIgnore
    private int precision;
    private int scale;

    public JdbcColumn(JqlSchema schema, ResultSetMetaData md, int col, ColumnBinder pkBinder, String comment, ArrayList<String> primaryKeys) throws SQLException {
        super(schema, md.getColumnName(col), resolveJavaType(md, col));

        this.isAutoIncrement = md.isAutoIncrement(col);
        this.isReadOnly = md.isReadOnly(col) | this.isAutoIncrement;
        this.isNullable = md.isNullable(col) != ResultSetMetaData.columnNoNulls;
        this.isPk = primaryKeys.contains(this.getColumnName());

        this.pkBinder = pkBinder;
        boolean isWritable = md.isWritable(col);
        if (!isWritable) {
            throw new RuntimeException("!isWritable");
        }
        this.colTypeName = md.getColumnTypeName(col);
        this.label = comment != null ? comment : md.getColumnLabel(col);
        this.precision = md.getPrecision(col);
        this.scale = md.getScale(col);
        this.displaySize = md.getColumnDisplaySize(col);
    }

    @Override
    public String getJsonKey() {
        if (fieldName == null) {
            fieldName = this.resolveFieldName();
        }
        return fieldName;
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
    public String getDBColumnType() {
        return colTypeName;
    }

    public JqlColumn getJoinedPrimaryColumn() {
        if (this.pkBinder == null) return null;
        JqlValueKind valueKind = this.getValueKind();
        Class javaType = null;
        if (!valueKind.isPrimitive()) {
            javaType = getJavaType();
            if (javaType == Object.class) {
                javaType = null;
            }
        }
        return pkBinder.getJoinedColumn(javaType);
    }

    protected void setMappedField(Field f) {
        super.setMappedField(f);
        this.fieldName = f.getName();
    }

    private String resolveFieldName() {
        StringBuilder sb = new StringBuilder();
        JqlColumn col = this;
        for (JqlColumn joinedPk; (joinedPk = col.getJoinedPrimaryColumn()) != null; col = joinedPk) {
            String token = JqlEntityJoin.initJsonKey(col);
            sb.append(token).append('.');
        }
        CharSequence rawFieldName = (col != this) ? sb.append(col.getColumnName()) : this.getColumnName();

        String name = getSchema().getSchemaLoader().getNameConverter().toLogicalAttributeName(rawFieldName.toString());
        return name;
    }


    protected void bindPrimaryKey(ColumnBinder pkBinder) {
        this.pkBinder = pkBinder;
    }

    private static Class resolveJavaType(ResultSetMetaData md, int col) throws SQLException {
        String colTypeName = md.getColumnTypeName(col);
        int colType = md.getColumnType(col);
        try {
            switch (colTypeName) {
                case "json":
                case "jsonb":
                    return Map.class;
                default:
                    String javaClassName = md.getColumnClassName(col);
                    return ClassUtils.getBoxedType(Class.forName(javaClassName));
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
