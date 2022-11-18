package org.slowcoders.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.SchemaLoader;
import org.slowcoders.jql.util.AttributeNameConverter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends JqlSchema {
    private HashMap<String, List<String>> uniqueConstraints = new HashMap<>();
    private final HashMap<String, List<JqlColumn>> fkConstraints = new HashMap<>();

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

    protected void initMappedColumns(Collection<List<JqlColumn>> mappedJoins) {
        super.initJsonKeys();
        for (List<JqlColumn> mc : fkConstraints.values()) {
            super.registerEntityJoin(new JqlSchemaJoin(this, mc));
        }
        for (List<JqlColumn> mc : mappedJoins) {
            super.registerEntityJoin(new JqlSchemaJoin(this, mc));

            Collection<JqlSchemaJoin> joins = mc.get(0).getSchema().getEntityJoins();
            for (JqlSchemaJoin j2 : joins) {
                if (mc != j2.getForeignKeyColumns() && !j2.isInverseMapped()) {
                    JqlSchemaJoin j3 = new JqlSchemaJoin(this, mc, j2);
                    super.registerEntityJoin(j3);
                }
            }
        }
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

    protected List<JqlColumn> makeForeignKeyConstraint(String fk_name) {
        List<JqlColumn> fkColumns = fkConstraints.get(fk_name);
        if (fkColumns == null) {
            fkColumns = new ArrayList<>();
            fkConstraints.put(fk_name, fkColumns);
        }
        return fkColumns;
    }

}
