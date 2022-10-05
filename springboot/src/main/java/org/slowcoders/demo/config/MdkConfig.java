package org.slowcoders.demo.config;

import org.slowcoders.jql.config.JQLConfig;
import org.slowcoders.jql.config.RestApiErrorHandler;
import org.slowcoders.jql.config.WebLoggingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

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