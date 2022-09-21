package org.slowcoders.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlColumnJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.ValueFormat;
import org.slowcoders.jql.util.ClassUtils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class MetaColumn extends JqlColumn {
    protected String colTypeName;
    @JsonIgnore
    protected int colType;
    protected int displaySize;
    @JsonIgnore
    protected JqlIndex index;

    @JsonIgnore
    protected int precision;
    protected int scale;

    public MetaColumn(JqlSchema schema, ResultSetMetaData md, int col, JqlColumnJoin fk, JqlIndex jqlIndex, String comment) throws SQLException {
        super(schema);
        this.columnName = md.getColumnName(col);
        this.fk = fk;
        this.fieldName = resolveFieldName();
        this.label = comment != null ? comment : md.getColumnLabel(col);
        this.index = jqlIndex;

        this.colTypeName = md.getColumnTypeName(col);
        this.colType = md.getColumnType(col);
        try {
            switch (this.colTypeName) {
                case "json":
                case "jsonb":
                    this.javaType = Object.class;
                    break;
                default:
                    String javaClassName = md.getColumnClassName(col);
                    this.javaType = ClassUtils.getBoxedType(Class.forName(javaClassName));
            }
            this.valueFormat = ValueFormat.resolveValueFormat(this.javaType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.isAutoIncrement = md.isAutoIncrement(col);
        this.isReadOnly = md.isReadOnly(col);
        boolean isWritable = md.isWritable(col);
        if (!isWritable) {
            throw new RuntimeException("!isWritable");
        }
        this.precision = md.getPrecision(col);
        this.scale = md.getScale(col);
        this.displaySize = md.getColumnDisplaySize(col);
        this.isNullable = md.isNullable(col) != ResultSetMetaData.columnNoNulls;
        this.isPk = false;
    }

    public String getDBColumnType() {
        return colTypeName;
    }

    public int getPrecision() {
        return precision;
    }

    @Override
    public int getScale() {
        return this.scale;
    }

    private JqlColumn getJoinedPrimaryColumn() {
        JqlColumnJoin fk = this.fk;
        if (fk == null) return null;
        JqlColumn pkCol = fk.loadPkSchema().getColumn(fk.getPkColumn());
        return pkCol;
    }

    private String resolveFieldName() {
        StringBuilder sb = new StringBuilder();
        JqlColumn col = this;
        for (JqlColumn joinedPk; (joinedPk = col.getJoinedPrimaryColumn()) != null; ) {
            col = joinedPk;
            sb.append(col.getSchema().getBaseTableName()).append('.');
        }
        CharSequence rawFieldName = (col != this) ? sb.append(this.getColumnName()) : col.getColumnName();

        String name = getSchema().getSchemaLoader().getNameConverter().toLogicalAttributeName(rawFieldName.toString());
        return name;
    }

}
