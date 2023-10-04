package org.slowcoders.hyperql.jdbc.output;

import org.slowcoders.hyperql.RestTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.List;

public interface JdbcResultMapper<T> extends ResultSetExtractor<List<T>> {
    void setOutputMetadata(RestTemplate.Response response);
}
