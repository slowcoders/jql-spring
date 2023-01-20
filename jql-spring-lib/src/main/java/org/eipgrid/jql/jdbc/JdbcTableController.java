package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlController;
import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.util.KVEntity;

public abstract class JdbcTableController<ID> extends JqlController.CRUD<KVEntity, ID> {

    public JdbcTableController(JqlService service, String tablePath) {
        super(service.makeRepository(tablePath));
    }
}
