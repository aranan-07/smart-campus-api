package com.smartcampus.filter;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 — API Request & Response Logging Filter
 *
 * Implements BOTH ContainerRequestFilter and ContainerResponseFilter in one class
 * to log every incoming request and every outgoing response.
 *
 * WHY FILTERS INSTEAD OF MANUAL LOGGING (Part 5.5 question answer):
 * Filters implement the "cross-cutting concern" pattern:
 *   1. DRY (Don't Repeat Yourself): One class handles logging for ALL endpoints.
 *      Without filters, you'd copy Logger.info() into every single resource method.
 *   2. Consistency: Every request is guaranteed to be logged — developers can't forget.
 *   3. Separation of concerns: Resource methods focus on business logic, not logging.
 *   4. Maintainability: To change log format, edit ONE file instead of dozens.
 *   5. Extensibility: The same pattern works for authentication, CORS headers,
 *      rate limiting, request ID injection, etc.
 * This is the same reason frameworks like Spring use AOP for cross-cutting concerns.
 *
 * @PreMatching means the filter runs before JAX-RS matches the request to a resource method,
 * which is important for logging — even 404 requests get logged.
 */
@Provider
@PreMatching
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Runs BEFORE the request reaches a resource method.
     * Logs the HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(String.format("[REQUEST]  --> %s %s", method, uri));
    }

    /**
     * Runs AFTER the resource method has returned a response.
     * Logs the HTTP status code of the outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        int    status = responseContext.getStatus();
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(String.format("[RESPONSE] <-- %d %s %s", status, method, uri));
    }
}
