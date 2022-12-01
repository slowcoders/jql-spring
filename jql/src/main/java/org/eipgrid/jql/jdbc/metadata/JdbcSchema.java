package org.eipgrid.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.SchemaLoader;
import org.eipgrid.jql.parser.SourceWriter;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends JqlSchema {
    private HashMap<String, List<String>> uniqueConstraints = new HashMap<>();
    private final HashMap<String, JqlSchemaJoin> fkConstraints = new HashMap<>();

    protected JdbcSchema(SchemaLoader schemaLoader, String tableName) {
        super(schemaLoader, tableName,
                AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(tableName.substring(tableName.indexOf('.') + 1)));
    }

    protected void init(ArrayList<? extends JqlColumn> columns, HashMap<String, List<String>> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
        super.init(columns);
    }

    public String dumpJPAEntitySchema() {
        SourceWriter sb = new SourceWriter('"');
        sb.writeln("import lombok.*;");
        sb.writeln("import javax.persistence.*;");
        sb.writeln("import javax.validation.constraints.*;");
        sb.writeln();

        sb.writeln("public class " + getTableName() + " {");
        for (JqlColumn col : getReadableColumns()) {
            dumpORM(col, sb);
            sb.writeln();
        }

        for (Map.Entry<String, JqlSchemaJoin> entry : this.getSchemaJoinMap().entrySet()) {
            sb.write("  jql.externalJoin(\"").write(entry.getKey()).write("\", ");
            sb.write(entry.getValue().getJoinedSchema().getSimpleTableName()).write("Schema, \n");
            sb.write(entry.getValue().isUniqueJoin() ? "{}" : "[]").write("),\n");
        }
        sb.write("];\n");

//        JqlColumn pk = col.getJoinedPrimaryColumn();
//        if (col.getJoinedPrimaryColumn() != null) {
//            sb.write(col.);
//            sb.write("@ManyToOne(fetch = FetchType.LAZY)");
//            sb.write("@JoinColumn(name = \"" + col.getColumnName() + "\", referencedColumnName = \"" + pk.getColumnName() + "\")")
//
//        }

        sb.writeln("}");
        return sb.toString();
    }

    private void dumpORM(JqlColumn col, SourceWriter sb) {
//        if (col.getLabel() != null) {
//            sb.write("\t/** ");
//            sb.write(col.getLabel());
//            sb.writeln(" */");
//        }
//        if (primaryKeys.contains(col.getColumnName())) {
//            sb.writeln("\t@Id");
//            if (col.isAutoIncrement()) {
//                sb.writeln("\t@GeneratedValue(strategy = GenerationType.IDENTITY)");
//            }
//        }
//        if (!col.isNullable()) {
//            sb.writeln("\t@NotNull");
//            sb.writeln("\t@Column(nullable = false)");
//        }
//        if (col.getPrecision() > 0) {
//            sb.writeln("\t@Max(" + col.getPrecision() +")");
//        }

        JqlColumn pk = col.getJoinedPrimaryColumn();
        if (pk != null) return;

        sb.write("\t@Getter");
        if (!col.isReadOnly()) {
            sb.write(" @Setter");
        }
        sb.writeln();
        String fieldName = col.getJavaFieldName();

        sb.writeln("\t" + col.getJavaType().getName() + " " + fieldName + ";");
    }


    public boolean isUniqueConstrainedColumnSet(List<JqlColumn> fkColumns) {
        int cntColumn = fkColumns.size();
        compare_constraint:
        for (List<String> uc : this.uniqueConstraints.values()) {
            if (uc.size() != cntColumn) continue;
            for (JqlColumn col : fkColumns) {
                String col_name = col.getColumnName();
                if (!uc.contains(col_name)) {
                    continue compare_constraint;
                }
            }
            return true;
        }
        return false;
    }

    public HashMap<String, JqlSchemaJoin> getForeignKeyConstraints() {
        return this.fkConstraints;
    }

    protected void addForeignKeyConstraint(String fk_name, JdbcColumn fkColumn) {
        JqlSchemaJoin fkJoin = fkConstraints.get(fk_name);
        List<JqlColumn> fkColumns;
        if (fkJoin == null) {
            fkColumns = new ArrayList<>();
            fkColumns.add(fkColumn);
            JqlSchemaJoin join = new JqlSchemaJoin(this, fkColumns);
            fkConstraints.put(fk_name, join);
        } else {
            fkColumns = fkJoin.getForeignKeyColumns();
            fkColumns.add(fkColumn);
        }
    }
}
