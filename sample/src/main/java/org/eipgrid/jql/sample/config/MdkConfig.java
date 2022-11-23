package org.eipgrid.jql.sample.config;

import org.eipgrid.jql.spring.config.JQLConfig;
import org.springframework.context.annotation.Configuration;

public class MdkConfig {
    @Configuration
    public static class JQL extends JQLConfig {
    }

//    @RestController
//    @ControllerAdvice
//    public static class RestApiError extends RestApiErrorHandler {
//    }

//    @Configuration
//    public static class WebLogging extends WebLoggingConfig {
//    }
}