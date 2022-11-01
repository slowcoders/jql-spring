package org.slowcoders.jql;

import java.util.ArrayList;
import java.util.List;

public class JqlEntityJoin {
    private String jsonName;
    private final boolean isUnique;
    private final boolean isInverseMapped;
    private final List<JqlColumn> joinedColumns;

    public JqlEntityJoin(List<JqlColumn> joinedColumns , boolean inverseMapped, boolean isUnique) {
        this.joinedColumns = joinedColumns;
        this.isInverseMapped = inverseMapped;
        this.isUnique = isUnique;
    }

    public List<JqlColumn> getJoinedColumns() {
        return joinedColumns;
    }

    public boolean isInverseMapped() {
        return this.isInverseMapped;
    }

    public boolean isUnique() {
        return this.isUnique;
    }

    public String getJsonName() {
        if (jsonName == null) {
            jsonName = resolveJsonName(joinedColumns);
        }
        return jsonName;
    }

    private String resolveJsonName(List<JqlColumn> joinedColumns) {
        JqlColumn first = joinedColumns.get(0);
        String name;
        if (this.isInverseMapped) {
            // column 이 없으므로 타입을 이용하여 이름을 정한다.
            name = first.getSchema().getEntityClassName();
        }
        else if (joinedColumns.size() == 1) {
            name = resolveJsonName(first);
        }
        else {
            name = first.getJoinedPrimaryColumn().getSchema().getEntityClassName();
        }
        return name;
    }

    public static String resolveJsonName(JqlColumn fk) {
        String fk_name = fk.getColumnName();
        JqlColumn joinedPk = fk.getJoinedPrimaryColumn();
        String pk_name = joinedPk.getColumnName();
        if (fk_name.endsWith("_" + pk_name)) {
            return fk_name.substring(0, fk_name.length() - pk_name.length() - 1);
        } else {
            return joinedPk.getSchema().getBaseTableName();
        }
    }

    public JqlSchema getJoinedSchema() {
        JqlColumn col = joinedColumns.get(0);
        if (!isInverseMapped) {
            col = col.getJoinedPrimaryColumn();
        }
        return col.getSchema();
    }

    public JqlSchema getAnchorSchema() {
        JqlColumn col = joinedColumns.get(0);
        if (isInverseMapped) {
            col = col.getJoinedPrimaryColumn();
        }
        return col.getSchema();
    }

    public boolean isJoinedBySingleKey() {
        return joinedColumns.size() == 1;
    }
}
