package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;

public class QueryBuilder extends SourceWriter<QueryBuilder> {
    private JqlSchema schema;

    public QueryBuilder(JqlSchema schema) {
        super('\'');
        this.schema = schema;
    }

    public JqlSchema setWorkingSchema(JqlSchema jqlSchema) {
        JqlSchema old = this.schema;
        this.schema = jqlSchema;
        return old;
    }

    public void writeColumnNames(Iterable<String> names, boolean withTableName) {
        for (String name : names) {
            writeColumnName(name, withTableName).write(", ");
        }
        shrinkLength(2);
    }

    public QueryBuilder writeEquals(String column, Object value) {
        this.write(column).write(" = ").writeValue(value);
        return this;
    }

    public QueryBuilder writeEquals(QAttribute column, Object value) {
        return writePredicate(column, " = ", value);
    }

    public QueryBuilder writePredicate(QAttribute column, String operator, Object value) {
        column.printSQL(this);
        this.write(operator).writeValue(value);
        return this;
    }

    public void writeWhere(JqlQuery where, boolean includeTableName) {
        if (includeTableName) {
            where.writeJoinStatement(this);
        }
        if (!where.isEmpty()) {
            writeRaw("\nWHERE ");
            where.buildQuery(this);
        }
    }

    public QueryBuilder writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            writeRaw(schema.getTableName()).write('.');
        }
        writeRaw(name);
        return this;
    }

    public QueryBuilder writeColumnName(String name) {
        return writeColumnName(name, true);
    }

    public QueryBuilder writeTableName() {
        writeRaw(this.schema.getTableName());
        return this;
    }


}
