package org.slowcoders.hyperql.sample;

import org.slowcoders.hyperql.jdbc.JdbcMetadataController;
import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hql/metadata")
public class MetadataController extends JdbcMetadataController {

    public MetadataController(JdbcStorage storage) {
        super(storage);
    }
}
