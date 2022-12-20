package org.eipgrid.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlEntityJoin;
import org.eipgrid.jql.SchemaLoader;
import org.eipgrid.jql.util.SourceWriter;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.persistence.*;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends JqlSchema {
    private HashMap<String, ArrayList<String>> uniqueConstraints = new HashMap<>();
    private final HashMap<String, JqlEntityJoin> fkConstraints = new HashMap<>();

    protected JdbcSchema(SchemaLoader schemaLoader, String tableName) {
        super(schemaLoader, tableName,
                AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(tableName.substring(tableName.indexOf('.') + 1)));
    }

    protected void init(ArrayList<? extends JqlColumn> columns, HashMap<String, ArrayList<String>> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
        super.init(columns);
    }

    public String dumpJPAEntitySchema() {
        SourceWriter sb = new SourceWriter('"');
        sb.writeln("import lombok.Getter;");
        sb.writeln("import lombok.Setter;");
        sb.writeln("import javax.persistence.*;");
        sb.writeln();

        sb.writeln("@Entity");
        sb.write("@Table(name = ").writeQuoted(this.getSimpleTableName()).
                write(", schema = ").writeQuoted(this.getNamespace()).write(", ");
        boolean isMultiPKs = false && getPKColumns().size() > 1;
        if (!this.uniqueConstraints.isEmpty()) {
            sb.incTab();
            sb.write("\nuniqueConstraints = {");
            sb.incTab();
            for (Map.Entry<String, ArrayList<String>> entry: this.uniqueConstraints.entrySet()) {
                sb.write("\n@UniqueConstraint(name =\"" + entry.getKey() + "\", columnNames = {");
                sb.incTab();
                for (String column : entry.getValue()) {
                    sb.writeQuoted(column).write(", ");
                }
                sb.replaceTrailingComma("}),");
                sb.decTab();
            }
            sb.decTab();
            sb.replaceTrailingComma("\n},\n");
            sb.decTab();
        }
        sb.replaceTrailingComma("\n)\n");

        String className = toJavaTypeName(getSimpleTableName());
        if (isMultiPKs) {
            sb.write("@IdClass(").write(className).writeln(".ID.class)");
        }
        sb.writeln("public class " + className + " implements java.io.Serializable {");
        if (isMultiPKs) {
            sb.incTab();
            sb.write("public static class ID implements Serializable {\n");
            sb.incTab();
            for (JqlColumn column : getPKColumns()) {
                dumpORM(column, sb);
            }
            sb.decTab();
            sb.decTab();
        }
        sb.incTab();
        //idColumns = getIDColumns();
        for (JqlColumn col : getReadableColumns()) {
            dumpORM(col, sb);
            sb.writeln();
        }

        for (Map.Entry<String, JqlEntityJoin> entry : this.getEntityJoinMap().entrySet()) {
            JqlEntityJoin join = entry.getValue();
            JqlSchema mappedSchema = join.getAssociatedSchema();
            boolean isInverseJoin = join.isInverseMapped();
            boolean isUniqueJoin = join.isUniqueJoin();
            boolean isArrayJoin = isInverseJoin && !isUniqueJoin;
            JqlColumn firstFk = join.getForeignKeyColumns().get(0);

            sb.write("@Getter @Setter\n");

            if (!isInverseJoin && firstFk.isPrimaryKey()) {
                sb.write("@Id\n");
            }
            if (isUniqueJoin) {
                sb.write("@OneToOne");
            } else if (isInverseJoin) {
                sb.write("@OneToMany");
            } else {
                sb.write("@ManyToOne");
            }

            sb.write("(fetch = FetchType.LAZY");
            if (isInverseJoin && join.getAssociativeJoin() == null) {
                String mappedField = firstFk.getJavaFieldName();
                sb.write(", mappedBy = ").writeQuoted(mappedField);
            }
            sb.write(")\n");

            if (!isInverseJoin) {
                JqlColumn fk = firstFk;
                sb.write("@JoinColumn(name = ").writeQuoted(fk.getColumnName()).write(", ");
                sb.write("referencedColumnName = ").writeQuoted(fk.getJoinedPrimaryColumn().getJavaFieldName()).write(")\n");
            }
            else if (join.getAssociativeJoin() != null) {
                sb.write("@JoinTable(name = ").writeQuoted(join.getJoinedSchema().getSimpleTableName()).write(", ");
                sb.write("joinColumns = @JoinColumn(name=").writeQuoted(firstFk.getColumnName()).write("), ");
                sb.write("inverseJoinColumns = @JoinColumn(name=").writeQuoted(join.getAssociativeJoin().getForeignKeyColumns().get(0).getColumnName()).write("))\n");
            }

            String mappedType = toJavaTypeName(mappedSchema.getSimpleTableName());
            if (!isArrayJoin) {
                sb.write(mappedType);
            } else {
                sb.write("List<").write(mappedType).write(">");
            }
            sb.write(" ").write(getJavaFieldName(join)).write(";\n\n");
        }
        sb.decTab();
        sb.writeln("}\n");
        return sb.toString();
    }

    private String toJavaTypeName(String name) {
        return AttributeNameConverter.toCamelCase(name, true);
    }

    private String getJavaFieldName(JqlEntityJoin join) {
        String name = join.getJsonKey();
        if (name.charAt(0) == '+') {
            name = name.substring(1);
        }
        return name;
    }

    private void dumpORM(JqlColumn col, SourceWriter sb) {
//        if (col.getLabel() != null) {
//            sb.write("\t/** ");
//            sb.write(col.getLabel());
//            sb.writeln(" */");
//        }
        if (col.getJoinedPrimaryColumn() != null) return;

        sb.write("@Getter");
        if (!col.isReadOnly()) {
            sb.write(" @Setter");
        }
        sb.writeln();

        if (col.isPrimaryKey()) {
            sb.writeln("@Id");
            if (col.isAutoIncrement()) {
                sb.writeln("@GeneratedValue(strategy = GenerationType.IDENTITY)");
            }
        }
        JqlColumn pk = col.getJoinedPrimaryColumn();
        if (pk != null) {
            boolean isUnique = this.isUniqueConstrainedColumnSet(Collections.singletonList(col));
            sb.write(isUnique ? "@One" : "@Many").writeln("ToOne(fetch = FetchType.LAZY)");
        }
        sb.write(pk != null ? "@Join" : "@").write("Column(name = ").writeQuoted(col.getColumnName()).write(", ");
        if (pk != null) {
            sb.write("referencedColumnName = ").writeQuoted(pk.getColumnName()).write(", ");
        }
        if (!col.isNullable()) {
            sb.writeln("nullable = false");
        }

        sb.replaceTrailingComma(")\n");

        String fieldName = col.getJavaFieldName();

        sb.write(col.getJavaType().getName()).write(" ").write(fieldName).writeln(";");
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

    public HashMap<String, JqlEntityJoin> getForeignKeyConstraints() {
        return this.fkConstraints;
    }

    protected void addForeignKeyConstraint(String fk_name, JdbcColumn fkColumn) {
        JqlEntityJoin fkJoin = fkConstraints.get(fk_name);
        List<JqlColumn> fkColumns;
        if (fkJoin == null) {
            fkColumns = new ArrayList<>();
            fkColumns.add(fkColumn);
            JqlEntityJoin join = new JqlEntityJoin(this, fkColumns);
            fkConstraints.put(fk_name, join);
        } else {
            fkColumns = fkJoin.getForeignKeyColumns();
            fkColumns.add(fkColumn);
        }
    }
}
