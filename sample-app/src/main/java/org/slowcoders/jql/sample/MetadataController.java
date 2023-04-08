package org.slowcoders.jql.sample;

import org.slowcoders.jql.jdbc.JdbcMetadataController;
import org.slowcoders.jql.jdbc.JdbcStorage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/metadata")
public class MetadataController extends JdbcMetadataController {

    public MetadataController(JdbcStorage storage) {
        super(storage);
    }
}
