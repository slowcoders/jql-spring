package org.eipgrid.jql.schema;

import java.util.List;
import java.util.stream.Collectors;

public class JQJoin {
    private final JQJoin associateJoin;
    private final String jsonKey;
    private final boolean isUnique;
    private final boolean inverseMapped;
    private final List<JQColumn> fkColumns;
    private final JQSchema baseSchema;
    private final JQSchema joinedSchema;

    public JQJoin(JQSchema baseSchema, List<JQColumn> fkColumns) {
        this(baseSchema, fkColumns, null);
    }

    public JQJoin(JQSchema baseSchema, List<JQColumn> fkColumns, JQJoin associatedJoin) {
        this.fkColumns = fkColumns;
        this.baseSchema = baseSchema;
        JQSchema fkSchema = fkColumns.get(0).getSchema();
        this.inverseMapped = baseSchema != fkSchema;
        if (inverseMapped) {
            assert(associatedJoin == null || !associatedJoin.inverseMapped);
            this.joinedSchema = fkSchema;
            this.isUnique = (associatedJoin == null || associatedJoin.isUnique) &&
                            fkSchema.isUniqueConstrainedColumnSet(fkColumns);
        } else {
            assert(associatedJoin == null);
            this.joinedSchema = fkColumns.get(0).getJoinedPrimaryColumn().getSchema();
            List<JQColumn> pkColumns = fkColumns.stream()
                    .map(col -> col.getJoinedPrimaryColumn())
                    .collect(Collectors.toList());
            this.isUnique = joinedSchema.isUniqueConstrainedColumnSet(pkColumns);
        }
        this.associateJoin = associatedJoin;
        this.jsonKey = associatedJoin != null ? '+' + associatedJoin.getJsonKey() : initJsonKey();
    }

    public List<JQColumn> getForeignKeyColumns() {
        return fkColumns;
    }

    public JQJoin getAssociativeJoin() {
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
        JQColumn first = fkColumns.get(0);
        String name;
        if (inverseMapped) {
            // column 이 없으므로 타입을 이용하여 이름을 정한다.
            name = // '+' +
                    first.getSchema().getEntityClassName();
        }
        else if (fkColumns.size() == 1) {
            name = initJsonKey(first);
        }
        else {
            name = first.getJoinedPrimaryColumn().getSchema().getEntityClassName();
        }
        return name;
    }

    public static String initJsonKey(JQColumn fk) {
        String fk_name = fk.getPhysicalName();
        JQColumn joinedPk = fk.getJoinedPrimaryColumn();
        String pk_name = joinedPk.getPhysicalName();
        if (fk_name.endsWith("_" + pk_name)) {
            return fk_name.substring(0, fk_name.length() - pk_name.length() - 1);
        } else {
            return joinedPk.getSchema().getSimpleTableName();
        }
    }

    public JQSchema getJoinedSchema() {
        JQColumn col = fkColumns.get(0);
        if (!inverseMapped) {
            col = col.getJoinedPrimaryColumn();
        }
        return col.getSchema();
    }

    public JQSchema getBaseSchema() {
        return this.baseSchema;
    }

    public boolean isJoinedBySingleKey() {
        return fkColumns.size() == 1;
    }

    public JQSchema getAssociatedSchema() {
        if (associateJoin != null) {
            return associateJoin.getJoinedSchema();
        } else {
            return this.getJoinedSchema();
        }
    }
}
