package org.slowcoders.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.SchemaLoader;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MetaTable extends JqlSchema {
    protected MetaTable(SchemaLoader schemaLoader, String tableName) {
        super(schemaLoader, tableName);
    }
}
