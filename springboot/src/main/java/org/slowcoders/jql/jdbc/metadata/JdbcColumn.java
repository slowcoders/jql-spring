package org.slowcoders.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slowcoders.jql.*;
import org.slowcoders.jql.util.ClassUtils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class JdbcColumn extends JqlColumn {

    protected boolean isReadOnly;
    protected boolean isAutoIncrement;
    protected boolean isNullable;
    protected boolean isPk;

    private String label;
    protected JqlColumnJoin fk;

    protected String colTypeName;
    @JsonIgnore
    protected int colType;
    protected int displaySize;
    @JsonIgnore
    protected JqlIndex index;

    @JsonIgnore
    protected int precision;
    protected int scale;

    public JdbcColumn(JqlSchema schema, ResultSetMetaData md, int col, JqlColumnJoin fk, JqlIndex jqlIndex, String comment) throws SQLException {
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
//        this.isPk = false;
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

    public JdbcColumn getJoinedPrimaryColumn() {
        JqlColumnJoin fk = this.fk;
        if (fk == null) return null;
        JqlColumn pkCol = fk.loadPkSchema().getColumn(fk.getPkColumn());
        return (JdbcColumn)pkCol;
    }

    private String resolveFieldName() {
        StringBuilder sb = new StringBuilder();
        JdbcColumn col = this;
        for (JdbcColumn joinedPk; (joinedPk = col.getJoinedPrimaryColumn()) != null; ) {
            col = joinedPk;
            sb.append(col.getSchema().getBaseTableName()).append('.');
        }
        CharSequence rawFieldName = (col != this) ? sb.append(this.getColumnName()) : col.getColumnName();

        String name = getSchema().getSchemaLoader().getNameConverter().toLogicalAttributeName(rawFieldName.toString());
        return name;
    }

    public JqlColumnJoin getJoinedForeignKey() {
        return fk;
    }
}
