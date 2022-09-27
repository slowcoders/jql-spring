package org.slowcoders.jql.jdbc.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.JqlSchemaJoin;
import org.slowcoders.jql.SchemaLoader;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JdbcSchema extends JqlSchema {
    protected JdbcSchema(SchemaLoader schemaLoader, String tableName) {
        super(schemaLoader, tableName,
                AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(tableName.substring(tableName.indexOf('.') + 1)));
    }

    protected boolean resolveJsonPath(ArrayList<String> fieldPath, String fieldName) {
        if (this.getJoinedForeignKeys(fieldName) != null) {
            fieldPath.add(fieldName);
            return true;
        }
        for (List<JqlColumn> fks : tableJoinMap.values()) {
            JdbcSchema pkSchema = (JdbcSchema) fks.get(0).getJoinedPrimaryColumn().getSchema();
            if (pkSchema.resolveJsonPath(fieldPath, fieldName)) {
                fieldPath.add(pkSchema.getJpaClassName());
                return true;
            }
        }
        return false;
    }

    public RowMapper<KVEntity> getColumnMapRowMapper() {
        return new JqlRowMapper(this);
    }

}
