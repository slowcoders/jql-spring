package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlEntityJoin;
import org.slowcoders.jql.JqlSchema;

import java.util.ArrayList;

class EntityJoinHelper {
    private final JqlSchema fkSchema;
    ArrayList<JqlColumn> fkColumns = new ArrayList<>();
    public EntityJoinHelper(JqlSchema fkSchema) {
        this.fkSchema = fkSchema;
    }

    public void addForeignKey(JqlColumn col) {
        fkColumns.add(col);
    }

    public void addMappedForeignKey(JqlColumn col) {
        JqlColumn pk = col.getJoinedPrimaryColumn();
        for (JqlColumn fk : fkColumns) {
            if (fk.getJoinedPrimaryColumn() == pk) {
                // character_friends 의 경우, character 에 대한 fk 가 두 개 존재한다.
                // 첫 번째 것만 처리한다.
                return;
            }
        }
        fkColumns.add(col);
    }

    JqlEntityJoin createMappedColumn(boolean inverseMapped) {
        boolean isUnique = fkSchema.isUnique(fkColumns);
        JqlEntityJoin join = new JqlEntityJoin(inverseMapped, isUnique);
        join.addAll(fkColumns);
        return join;
    }
}
