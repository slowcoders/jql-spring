package org.slowcoders.jql.jdbc.timescale;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface Aggregate {
    Type value();

    enum Type {
        None,
        Mean,
        Sum,
    }
}
