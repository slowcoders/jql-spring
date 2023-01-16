package org.eipgrid.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.eipgrid.jql.schema.JQColumn;
import org.eipgrid.jql.schema.JQSchema;
import org.eipgrid.jql.schema.JQJoin;
import org.eipgrid.jql.schema.JQSchemaLoader;
import org.eipgrid.jql.util.SourceWriter;
import org.eipgrid.jql.util.AttributeNameConverter;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends JQSchema {
    private HashMap<String, ArrayList<String>> uniqueConstraints = new HashMap<>();
    private final HashMap<String, JQJoin> fkConstraints = new HashMap<>();

    protected JdbcSchema(JQSchemaLoader schemaLoader, String tableName) {
        super(schemaLoader, tableName,
                AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(tableName.substring(tableName.indexOf('.') + 1)));
    }

    protected void init(ArrayList<? extends JQColumn> columns, HashMap<String, ArrayList<String>> uniqueConstraints, Class<?> ormType) {
        this.uniqueConstraints = uniqueConstraints;
        super.init(columns, ormType);
    }

    protected void initJsonKeys(Class<?> ormType) {
        if (ormType != null) {
            initFieldNames(ormType);
        }
        super.initJsonKeys(ormType);
    }

    private void initFieldNames(Class<?> ormType) {
        Class<?> superClass = ormType.getSuperclass();
        if (superClass != Object.class) {
            initFieldNames(superClass);
        }
        for (Field f : ormType.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.TRANSIENT) == 0 &&
                    f.getAnnotation(Transient.class) == null) {
                String colName = resolveColumnName(f);
                JQColumn column = this.getColumn(colName);
                super.mapColumn(column, f);
            }
        }
    }

    private String resolveColumnName(Field f) {
        if (true) {
            Column c = f.getAnnotation(Column.class);
            if (c != null) {
                String colName = c.name();
                if (colName != null && colName.length() > 0) {
                    return colName;
                }
            }
        }
        if (true) {
            JoinColumn c = f.getAnnotation(JoinColumn.class);
            if (c != null) {
                String colName = c.name();
                if (colName != null && colName.length() > 0) {
                    return colName;
                }
            }
        }

        AttributeNameConverter cvt = getSchemaLoader().getNameConverter();
        String colName = cvt.toPhysicalColumnName(f.getName());
        return colName;
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
            for (JQColumn column : getPKColumns()) {
                dumpORM(column, sb);
            }
            sb.decTab();
            sb.decTab();
        }
        sb.incTab();
        //idColumns = getIDColumns();
        for (JQColumn col : getReadableColumns()) {
            dumpORM(col, sb);
            sb.writeln();
        }

        for (Map.Entry<String, JQJoin> entry : this.getEntityJoinMap().entrySet()) {
            JQJoin join = entry.getValue();
            JQSchema mappedSchema = join.getAssociatedSchema();
            boolean isInverseJoin = join.isInverseMapped();
            boolean isUniqueJoin = join.isUniqueJoin();
            boolean isArrayJoin = isInverseJoin && !isUniqueJoin;
            JQColumn firstFk = join.getForeignKeyColumns().get(0);

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
                String mappedField = firstFk.resolveJavaFieldName();
                sb.write(", mappedBy = ").writeQuoted(mappedField);
            }
            sb.write(")\n");

            if (!isInverseJoin) {
                JQColumn fk = firstFk;
                sb.write("@JoinColumn(name = ").writeQuoted(fk.getPhysicalName()).write(", ");
                sb.write("referencedColumnName = ").writeQuoted(fk.getJoinedPrimaryColumn().resolveJavaFieldName()).write(")\n");
            }
            else if (join.getAssociativeJoin() != null) {
                sb.write("@JoinTable(name = ").writeQuoted(join.getJoinedSchema().getSimpleTableName()).write(", ");
                sb.write("joinColumns = @JoinColumn(name=").writeQuoted(firstFk.getPhysicalName()).write("), ");
                sb.write("inverseJoinColumns = @JoinColumn(name=").writeQuoted(join.getAssociativeJoin().getForeignKeyColumns().get(0).getPhysicalName()).write("))\n");
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

    private String getJavaFieldName(JQJoin join) {
        String name = join.getJsonKey();
        if (name.charAt(0) == '+') {
            name = name.substring(1);
        }
        return name;
    }

    private void dumpORM(JQColumn col, SourceWriter sb) {
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
        JQColumn pk = col.getJoinedPrimaryColumn();
        if (pk != null) {
            boolean isUnique = this.isUniqueConstrainedColumnSet(Collections.singletonList(col));
            sb.write(isUnique ? "@One" : "@Many").writeln("ToOne(fetch = FetchType.LAZY)");
        }
        sb.write(pk != null ? "@Join" : "@").write("Column(name = ").writeQuoted(col.getPhysicalName()).write(", ");
        if (pk != null) {
            sb.write("referencedColumnName = ").writeQuoted(pk.getPhysicalName()).write(", ");
        }
        if (!col.isNullable()) {
            sb.writeln("nullable = false");
        }

        sb.replaceTrailingComma(")\n");

        String fieldName = col.resolveJavaFieldName();

        sb.write(col.getJavaType().getName()).write(" ").write(fieldName).writeln(";");
    }


    public boolean isUniqueConstrainedColumnSet(List<JQColumn> fkColumns) {
        int cntColumn = fkColumns.size();
        compare_constraint:
        for (List<String> uc : this.uniqueConstraints.values()) {
            if (uc.size() != cntColumn) continue;
            for (JQColumn col : fkColumns) {
                String col_name = col.getPhysicalName();
                if (!uc.contains(col_name)) {
                    continue compare_constraint;
                }
            }
            return true;
        }
        return false;
    }

    public HashMap<String, JQJoin> getForeignKeyConstraints() {
        return this.fkConstraints;
    }

    protected void addForeignKeyConstraint(String fk_name, JdbcColumn fkColumn) {
        JQJoin fkJoin = fkConstraints.get(fk_name);
        List<JQColumn> fkColumns;
        if (fkJoin == null) {
            fkColumns = new ArrayList<>();
            fkColumns.add(fkColumn);
            JQJoin join = new JQJoin(this, fkColumns);
            fkConstraints.put(fk_name, join);
        } else {
            fkColumns = fkJoin.getForeignKeyColumns();
            fkColumns.add(fkColumn);
        }
    }
}
