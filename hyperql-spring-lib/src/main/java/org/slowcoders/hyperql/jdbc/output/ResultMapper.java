package org.slowcoders.hyperql.jdbc.output;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.List;
import java.util.Properties;

public interface ResultMapper extends ResultSetExtractor<List<?>> {
    void fillProperties(Properties properties);
}
