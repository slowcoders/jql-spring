package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlController;
import org.eipgrid.jql.util.KVEntity;

public abstract class JdbcTableController<ID> extends JqlController.SearchAndUpdate<KVEntity, ID> {

    public JdbcTableController(JdbcJqlService service, String tablePath) {
        super(service.makeRepository(tablePath));
    }
}
