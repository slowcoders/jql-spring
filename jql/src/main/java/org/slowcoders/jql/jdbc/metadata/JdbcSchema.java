package org.slowcoders.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.SchemaLoader;
import org.slowcoders.jql.util.AttributeNameConverter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends JqlSchema {
    private List<String[]> uniqueConstraints;

    protected JdbcSchema(SchemaLoader schemaLoader, String tableName) {
        super(schemaLoader, tableName,
                AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(tableName.substring(tableName.indexOf('.') + 1)));
    }

    protected void init(ArrayList<? extends JqlColumn> columns, List<String[]> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
        super.init(columns);
    }

    @Override
    public boolean isUnique(List<JqlColumn> fkColumns) {
        int cntColumn = fkColumns.size();
        for (String[] uc : this.uniqueConstraints) {
            if (uc.length != cntColumn) continue;
            int cntMatch = 0;
            for (JqlColumn column : fkColumns) {
                String colName = column.getColumnName();
                for (String s : uc) {
                    if (s.equals(colName)) {
                        cntMatch ++;
                        break;
                    }
                }
            }
            if (cntMatch == cntColumn) return true;
        }
        return false;
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

        out.println("\t" + col.getJavaType().getName() + " " + col.getJsonName() + ";");
    }

    protected void initMappedColumns(ArrayList<JqlEntityJoin> joinedColumns, ArrayList<JqlEntityJoin> mappedColumns) {//String key, ArrayList<JqlColumn> mappedColumns) {
        super.initJsonNames();
        for (JqlEntityJoin mc : joinedColumns) {
            super.initMappedColumns(mc.getJsonName(), mc);
        }
        for (JqlEntityJoin mc : mappedColumns) {
            super.initMappedColumns(mc.getJsonName(), mc);
        }
    }
}
