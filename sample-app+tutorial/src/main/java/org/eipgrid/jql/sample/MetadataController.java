package org.eipgrid.jql.sample;

import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.eipgrid.jql.jdbc.JdbcMetadataController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/metadata")
public class MetadataController extends JdbcMetadataController {

    public MetadataController(JqlService service) {
        super(service);
    }
}
