package org.eipgrid.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QJoin;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.schema.SchemaLoader;
import org.eipgrid.jql.util.CaseConverter;
import org.eipgrid.jql.util.ClassUtils;
import org.eipgrid.jql.util.SourceWriter;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.lang.reflect.Field;
import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends QSchema {
    private HashMap<String, ArrayList<String>> uniqueConstraints = new HashMap<>();
    private final HashMap<String, List<QColumn>> fkConstraints = new HashMap<>();

    private ArrayList<QColumn> unresolvedJpaColumns;

    protected JdbcSchema(SchemaLoader schemaLoader, String tableName, Class<?> ormType) {
        super(schemaLoader, tableName, ormType);
    }

    protected JdbcSchema(SchemaLoader schemaLoader, String tableName) {
        this(schemaLoader, tableName, Map.class);
    }

    protected void init(ArrayList<? extends QColumn> columns, HashMap<String, ArrayList<String>> uniqueConstraints, Class<?> ormType) {
        this.uniqueConstraints = uniqueConstraints;

        if (!Map.class.isAssignableFrom(ormType)) {
            HashMap<String, Field> jpaColumns = new HashMap<>();
            for (Field f: ClassUtils.getInstanceFields(ormType, true)) {
                jpaColumns.put(resolvePhysicalName(f), f);
            }

            for (int i = columns.size(); --i >= 0; ) {
                QColumn col = columns.get(i);
                Field f = jpaColumns.get(col.getPhysicalName());
                if (f == null) {
                    columns.remove(i);
                    if (unresolvedJpaColumns == null) unresolvedJpaColumns = new ArrayList<>();
                    unresolvedJpaColumns.add(col);
                }
                else {
                    super.mapColumn(col, f);
                }
            }
        }
        super.init(columns, ormType);
    }

//    protected void initJsonKeys(Class<?> ormType) {
//        if (ormType != null) {
//            initFieldNames(ormType);
//        }
//        super.initJsonKeys(ormType);
//    }
//
//    private void initFieldNames(Class<?> ormType) {
//        Class<?> superClass = ormType.getSuperclass();
//        if (superClass != Object.class) {
//            initFieldNames(superClass);
//        }
//        for (Field f : ormType.getDeclaredFields()) {
//            if ((f.getModifiers() & Modifier.TRANSIENT) == 0 &&
//                    f.getAnnotation(Transient.class) == null) {
//                String colName = resolveColumnName(f);
//                QColumn column = this.getColumn(colName);
//                super.mapColumn(column, f);
//            }
//        }
//    }
//
//    private String resolveColumnName(Field f) {
//        if (true) {
//            Column c = f.getAnnotation(Column.class);
//            if (c != null) {
//                String colName = c.name();
//                if (colName != null && colName.length() > 0) {
//                    return colName;
//                }
//            }
//        }
//        if (true) {
//            JoinColumn c = f.getAnnotation(JoinColumn.class);
//            if (c != null) {
//                String colName = c.name();
//                if (colName != null && colName.length() > 0) {
//                    return colName;
//                }
//            }
//        }
//
//        AttributeNameConverter cvt = getSchemaLoader().getNameConverter();
//        String colName = cvt.toPhysicalColumnName(f.getName());
//        return colName;
//    }
//


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
            for (QColumn column : getPKColumns()) {
                dumpORM(column, sb);
            }
            sb.decTab();
            sb.decTab();
        }
        sb.incTab();
        //idColumns = getIDColumns();
        for (QColumn col : getReadableColumns()) {
            dumpORM(col, sb);
            sb.writeln();
        }

        for (Map.Entry<String, QJoin> entry : this.getEntityJoinMap().entrySet()) {
            QJoin join = entry.getValue();
            QSchema mappedSchema = join.getTargetSchema();
            boolean isInverseJoin = join.isInverseMapped();
            boolean isUniqueJoin = join.isUniqueJoin();
            boolean isArrayJoin = isInverseJoin && !isUniqueJoin;
            QColumn firstFk = join.getForeignKeyColumns().get(0);

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
                String mappedField = getJavaFieldName(firstFk);
                sb.write(", mappedBy = ").writeQuoted(mappedField);
            }
            sb.write(")\n");

            if (!isInverseJoin) {
                QColumn fk = firstFk;
                sb.write("@JoinColumn(name = ").writeQuoted(fk.getPhysicalName()).write(", ");
                sb.write("referencedColumnName = ").writeQuoted(getJavaFieldName(fk.getJoinedPrimaryColumn())).write(")\n");
            }
            else if (join.getAssociativeJoin() != null) {
                sb.write("@JoinTable(name = ").writeQuoted(join.getLinkedSchema().getSimpleTableName()).write(", ");
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
        return CaseConverter.toCamelCase(name, true);
    }

    private String getJavaFieldName(QJoin join) {
        String name = join.getJsonKey();
        if (name.charAt(0) == '+') {
            name = name.substring(1);
        }
        return name;
    }

    private void dumpORM(QColumn col, SourceWriter sb) {
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
        QColumn pk = col.getJoinedPrimaryColumn();
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

        String fieldName = getJavaFieldName(col);

        sb.write(col.getValueType().toJavaClass().getName()).write(" ").write(fieldName).writeln(";");
    }


    public boolean isUniqueConstrainedColumnSet(List<QColumn> fkColumns) {
        int cntColumn = fkColumns.size();
        compare_constraint:
        for (List<String> uc : this.uniqueConstraints.values()) {
            if (uc.size() != cntColumn) continue;
            for (QColumn col : fkColumns) {
                String col_name = col.getPhysicalName();
                if (!uc.contains(col_name)) {
                    continue compare_constraint;
                }
            }
            return true;
        }
        return false;
    }

    protected QJoin getJoinByForeignKeyConstraints(String fkConstraint) {
        List<QColumn> fkColumns = this.fkConstraints.get(fkConstraint);
        for (QJoin join : this.getEntityJoinMap().values()) {
            if (join.getForeignKeyColumns() == fkColumns) {
                assert(join.getBaseSchema() == this && !join.isInverseMapped());
                return join;
            }
        }
        throw new RuntimeException("fk join not found: " + fkConstraint);
    }

    HashMap<String, List<QColumn>> getForeignKeyConstraints() {
        return this.fkConstraints;
    }
    protected void addForeignKeyConstraint(String fk_name, JdbcColumn fkColumn) {
        List<QColumn> fkColumns = fkConstraints.get(fk_name);
        if (fkColumns == null) {
            fkColumns = new ArrayList<>();
            fkColumns.add(fkColumn);
            fkConstraints.put(fk_name, fkColumns);
        } else {
            fkColumns.add(fkColumn);
        }
    }

    private String resolvePhysicalName(Field f) {
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
        CaseConverter cvt = getSchemaLoader().getNameConverter();
        String colName = cvt.toPhysicalColumnName(f.getName());
        return colName;
    }
}
