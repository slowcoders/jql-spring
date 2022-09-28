package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;

import java.util.Collection;

public class SQLWriter {
    private final StringBuilder sb = new StringBuilder();
    private JqlSchema schema;
    private int tab;

    public SQLWriter(JqlSchema schema) {
        this.schema = schema;
    }

    public JqlSchema pushJoinedTable(JqlSchema jqlSchema) {
        JqlSchema old = this.schema;
        this.schema = jqlSchema;
        return old;
    }

    public void replaceTableInfo(JqlSchema jqlSchema) {
        this.schema = jqlSchema;
    }

    public SQLWriter replaceTrailingComma(String text) {
        int len = sb.length();
        while (len > 0) {
            char ch = sb.charAt(--len);
            if (ch <= ' ') continue;
            if (ch == ',') break;
        }
        sb.setLength(len);
        this.write(text);
        return this;
    }

    public void writeColumnNames(Iterable<String> names, boolean withTableName) {
        for (String name : names) {
            writeColumnName(name, withTableName).write(", ");
        }
        sb.setLength(sb.length() - 2);
    }

    public SQLWriter writeln(String statement) {
        this.write(statement);
        this.writeln();
        return this;
    }

    public SQLWriter incTab() {
        this.tab ++;
        return this;
    }

    public SQLWriter decTab() {
        if (this.tab > 0) {
            this.tab--;
        }
        return this;
    }

    public SQLWriter write(char ch) {
        char last_ch = sb.length() == 0 ? 0 : sb.charAt(sb.length()-1);
        if (last_ch == '\n') {
            for (int t = tab; --t >= 0; ) {
                sb.append('\t');
            }
        }
        sb.append(ch);
        return this;
    }

    public SQLWriter write(String statement) {
        for (int i = 0; i < statement.length(); i ++) {
            char ch = statement.charAt(i);
            write(ch);
        }
        return this;
    }

    public void writeln() {
        sb.append('\n');
    }

    public SQLWriter writeF(String statement, String... params) {
        for (int i = 0; i < statement.length(); i ++) {
            char ch = statement.charAt(i);
            if (ch != '{') {
                write(ch);
                continue;
            }

            int idx = 0;
            while (true) {
                ch = statement.charAt(++i);
                if (ch >= 0 && ch <= '9') {
                    idx = idx * 10 + ch - '0';
                }
                if (ch == '}') break;
            }
            write(params[idx]);
        }
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public SQLWriter writeValue(Object v) {
        if (v != null && isQuotedColumn(v)) {
            sb.append('\'').append(v).append('\'');
        }
        else {
            sb.append(v);
        }
        return this;
    }

    public SQLWriter writeValues(Collection values) {
        if (values == null || values.size() == 0) {
            return this;
        }

        for (Object v : values) {
            if (isQuotedColumn(v)) {
                sb.append('\'').append(v.toString()).append('\'').append(", ");
            }
            else {
                sb.append(v.toString()).append(", ");
            }
        }
        sb.setLength(sb.length() - 2);
        return this;
    }

    private boolean isQuotedColumn(Object value) {
        if (value == null) return false;
        Class<?> type = value.getClass();
        return !Number.class.isAssignableFrom(type) &&
                !Boolean.class.isAssignableFrom(type);
    }

    public SQLWriter writeEquals(String column, Object value) {
        this.write(column).write(" = ").writeValue(value);
        return this;
    }

    public SQLWriter writeEquals(QAttribute column, Object value) {
        return writePredicate(column, " = ", value);
    }

    public SQLWriter writePredicate(QAttribute column, String operator, Object value) {
        column.printSQL(this);
        this.write(operator).writeValue(value);
        return this;
    }

    public void writeWhere(JqlQuery where, boolean includeTableName) {
        if (includeTableName) {
            where.writeJoinStatement(this);
        }
        if (!where.isEmpty()) {
            sb.append("\nWHERE ");
            where.printSQL(this);
        }
    }

    public SQLWriter writeColumnName(String name, boolean withTableName) {
        if (withTableName) {
            sb.append(schema.getTableName()).append('.');
        }
        sb.append(name);
        return this;
    }

    public SQLWriter writeColumnName(String name) {
        return writeColumnName(name, true);
    }

    public SQLWriter writeTableName() {
        sb.append(this.schema.getTableName());
        return this;
    }

    public SQLWriter shrinkLength(int len) {
        sb.setLength(sb.length() - len);
        return this;
    }

}
