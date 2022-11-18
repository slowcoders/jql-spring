package org.slowcoders.jql;

import java.util.List;
import java.util.stream.Collectors;

public class JqlSchemaJoin {
    private final JqlSchemaJoin associateJoin;
    private final String jsonKey;
    private final boolean isUnique;
    private final boolean inverseMapped;
    private final List<JqlColumn> fkColumns;
    private final JqlSchema baseSchema;
    private final JqlSchema joinedSchema;

    public JqlSchemaJoin(JqlSchema baseSchema, List<JqlColumn> fkColumns) {
        this(baseSchema, fkColumns, null);
    }

    public JqlSchemaJoin(JqlSchema baseSchema, List<JqlColumn> fkColumns, JqlSchemaJoin associatedJoin) {
        this.fkColumns = fkColumns;
        this.baseSchema = baseSchema;
        this.inverseMapped = baseSchema != fkColumns.get(0).getSchema();
        if (inverseMapped) {
            if (!(associatedJoin == null || !associatedJoin.inverseMapped)) {
                throw new RuntimeException("invalid associatedJoin");
            }
            this.joinedSchema = fkColumns.get(0).getSchema();
            this.isUnique = (associatedJoin == null || associatedJoin.isUnique)
                            && joinedSchema.isUniqueConstrainedColumnSet(fkColumns);
        } else {
            if (associatedJoin != null) {
                throw new RuntimeException("invalid associatedJoin");
            }
            List<JqlColumn> pkColumns = fkColumns.stream()
                    .map(col -> col.getJoinedPrimaryColumn())
                    .collect(Collectors.toList());
            this.joinedSchema = pkColumns.get(0).getSchema();
            this.isUnique = joinedSchema.isUniqueConstrainedColumnSet(pkColumns);
        }
        this.associateJoin = associatedJoin;
        this.jsonKey = associatedJoin != null ? '+' + associatedJoin.getJsonKey() : initJsonKey();
    }

    public List<JqlColumn> getForeignKeyColumns() {
        return fkColumns;
    }

    public JqlSchemaJoin getAssociativeJoin() {
        return associateJoin;
    }

    public boolean isInverseMapped() {
        return this.inverseMapped;
    }

    public boolean isUniqueJoin() {
        return this.isUnique;
    }

    public String getJsonKey() {
        return jsonKey;
    }

    private String initJsonKey() {
        if (this.jsonKey != null) {
            throw new RuntimeException("already initialized");
        }
        JqlColumn first = fkColumns.get(0);
        String name;
        if (inverseMapped) {
            // column 이 없으므로 타입을 이용하여 이름을 정한다.
            name = first.getSchema().getEntityClassName();
        }
        else if (fkColumns.size() == 1) {
            name = initJsonKey(first);
        }
        else {
            name = first.getJoinedPrimaryColumn().getSchema().getEntityClassName();
        }
        return name;
    }

    public static String initJsonKey(JqlColumn fk) {
        String fk_name = fk.getColumnName();
        JqlColumn joinedPk = fk.getJoinedPrimaryColumn();
        String pk_name = joinedPk.getColumnName();
        if (fk_name.endsWith("_" + pk_name)) {
            return fk_name.substring(0, fk_name.length() - pk_name.length() - 1);
        } else {
            return joinedPk.getSchema().getSimpleTableName();
        }
    }

    public JqlSchema getJoinedSchema() {
        JqlColumn col = fkColumns.get(0);
        if (!inverseMapped) {
            col = col.getJoinedPrimaryColumn();
        }
        return col.getSchema();
    }

    public JqlSchema getBaseSchema() {
        return this.baseSchema;
    }

    public boolean isJoinedBySingleKey() {
        return fkColumns.size() == 1;
    }

    public JqlSchema getAssociatedSchema() {
        if (associateJoin != null) {
            return associateJoin.getJoinedSchema();
        } else {
            return this.getJoinedSchema();
        }
    }
}
