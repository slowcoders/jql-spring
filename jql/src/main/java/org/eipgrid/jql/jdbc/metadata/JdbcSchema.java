package org.eipgrid.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.SchemaLoader;
import org.eipgrid.jql.util.AttributeNameConverter;

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        out.println("import lombok.*;");
        out.println("import javax.persistence.*;");
        out.println("import javax.validation.constraints.*;");
        out.println();

        out.println("public class " + getTableName() + " {");
        for (JqlColumn col : getReadableColumns()) {
            dumpORM(col, out);
            out.println();
        }
        out.println("}");
        return baos.toString();
    }

    private void dumpORM(JqlColumn col, PrintStream out) {
//        if (col.getLabel() != null) {
//            out.print("\t/** ");
//            out.print(col.getLabel());
//            out.println(" */");
//        }
//        if (primaryKeys.contains(col.getColumnName())) {
//            out.println("\t@Id");
//            if (col.isAutoIncrement()) {
//                out.println("\t@GeneratedValue(strategy = GenerationType.IDENTITY)");
//            }
//        }
//        if (!col.isNullable()) {
//            out.println("\t@NotNull");
//            out.println("\t@Column(nullable = false)");
//        }
//        if (col.getPrecision() > 0) {
//            out.println("\t@Max(" + col.getPrecision() +")");
//        }

        out.print("\t@Getter");
        if (!col.isReadOnly()) {
            out.print(" @Setter");
        }
        out.println();

        out.println("\t" + col.getJavaType().getName() + " " + col.getJsonKey() + ";");
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
