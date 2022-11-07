package org.slowcoders.jql;

import java.util.ArrayList;
import java.util.List;

public class JqlEntityJoin {
    private String jsonKey;
    private boolean isUnique;
    private boolean isInverseMapped;
    private final List<JqlColumn> joinedColumns;
    private String constraintName;

    public JqlEntityJoin(String constraintName) {//boolean inverseMapped, boolean isUnique) {
        this.joinedColumns = new ArrayList<>();
        this.constraintName = constraintName;
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

    public String getJsonKey() {
        assert (jsonKey != null);
        return jsonKey;
    }

    public void addForeignKey(JqlColumn fk) {
        joinedColumns.add(fk);
    }

    public String initJsonKey(boolean isInverseMapped) {
        JqlColumn first = joinedColumns.get(0);
        this.isInverseMapped = isInverseMapped;
        String name;
        if (isInverseMapped) {
            // column 이 없으므로 타입을 이용하여 이름을 정한다.
            name = first.getSchema().getEntityClassName();
        }
        else if (joinedColumns.size() == 1) {
            name = initJsonKey(first);
        }
        else {
            name = first.getJoinedPrimaryColumn().getSchema().getEntityClassName();
        }
        return (this.jsonKey = name);
    }

    public static String initJsonKey(JqlColumn fk) {
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

    public String getConstraintName() {
        return this.constraintName;
    }
}
