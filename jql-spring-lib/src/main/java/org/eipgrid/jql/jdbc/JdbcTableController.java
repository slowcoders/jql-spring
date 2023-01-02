package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.spring.JQController;
import org.eipgrid.jql.util.KVEntity;

public abstract class JdbcTableController<ID> extends JQController.SearchAndUpdate<KVEntity, ID> {

    public JdbcTableController(JdbcJQService service, String tablePath) {
        super(service.makeRepository(tablePath));
    }
}
