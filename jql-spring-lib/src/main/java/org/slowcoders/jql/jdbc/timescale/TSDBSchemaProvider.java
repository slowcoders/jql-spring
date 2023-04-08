package org.slowcoders.jql.jdbc.timescale;

import java.util.HashMap;

public interface TSDBSchemaProvider {
    String generateDDL(String tableName);
    HashMap<String, AggregateType> getAggregationTypeMap();
}
