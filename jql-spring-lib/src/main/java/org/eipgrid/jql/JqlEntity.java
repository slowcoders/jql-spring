package org.eipgrid.jql;

import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.util.KVEntity;

import java.util.Map;

public class JqlEntity extends KVEntity implements Map<String, Object> {

    public static class CRUDController<ID> extends JqlController.CRUD<JqlEntity, ID> {
        public CRUDController(JqlRepository<JqlEntity, ID> repository) {
            super(repository);
        }
    }

    public static class Repository extends JDBCRepositoryBase<JqlEntity, Object> {

        public Repository(JqlService service, String tableName) {
            super(service, service.loadSchema(tableName, null));
        }
    }
}
