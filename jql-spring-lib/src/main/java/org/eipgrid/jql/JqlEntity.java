package org.eipgrid.jql;

import org.eipgrid.jql.jdbc.JDBCRepositoryBase;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.util.KVEntity;

import java.util.List;
import java.util.Map;

public class JqlEntity extends KVEntity implements Map<String, Object> {

    public static class SearchController<ID> extends JqlController.Search<ID> {
        public SearchController(JqlEntityStore<ID> store) {
            super(store);
        }
    }

    public static class CRUDController<ID> extends JqlController.CRUD<ID> {
        public CRUDController(JqlEntityStore<ID> store) {
            super(store);
        }
    }

    public static class Repository<ID> extends JDBCRepositoryBase<JqlEntity, ID> {
        public Repository(JqlService service, QSchema schema) {
            super(service, schema);
        }
    }
}
