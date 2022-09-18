package org.slowcoders.jql.jdbc.parser;

//public class QJoinedScope {
//    private final ForeignKey fk;
//    private final JqlSchema schema;
//    private final ArrayList<QJoinedScope> joinedNodes = new ArrayList<>();
//
//    QJoinedScope(JqlSchema schema, ForeignKey fk) {
//        this.schema = schema;
//        this.fk = fk;
//    }
//
//    public QJoinedScope(ForeignKey fk) {
//        this(fk.getPkTable(), fk);
//    }
//
//    public JqlSchema getSchema() {
//        return schema;
//    }
//
//    public void writeJoinStatement(SQLWriter sb) {
//        String tableName = schema.getTableName();
//        sb.write(tableName);
//        if (!joinedNodes.isEmpty()) {
//            for (QJoinedScope subJoin : joinedNodes) {
//                ForeignKey fk = subJoin.fk;
//                String primaryTable = fk.getPkTable().getTableName();
//                sb.write(" left outer join ").write(primaryTable);
//                sb.write(" on ").write(primaryTable).write(".").write(fk.getPkColumn());
//                sb.write(" = ").write(tableName).write(".").write(fk.getFkColumn());
//            }
//        }
//    }
//
//    public void addSubNode(QJoinedScope new_join) {
//        this.joinedNodes.add(new_join);
//    }
//}
