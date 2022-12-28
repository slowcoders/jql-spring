package org.eipgrid.jql.jdbc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eipgrid.jql.spring.JQLController;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JdbcTableController<ID> extends JQLController.SearchAndUpdate<KVEntity, ID> {

    public JdbcTableController(JQLJdbcService service, String tablePath) {
        super(service.makeRepository(tablePath));
    }
}
