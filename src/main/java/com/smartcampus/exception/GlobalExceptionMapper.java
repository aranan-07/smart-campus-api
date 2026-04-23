package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 — Global Safety Net Exception Mapper
 *
 * Catches ANY uncaught Throwable (NullPointerException, IndexOutOfBoundsException, etc.)
 * and returns a safe, generic HTTP 500 response with NO internal stack trace details.
 *
 * WHY NEVER EXPOSE STACK TRACES (Part 5.4 question answer):
 * Stack traces reveal:
 *   1. Full class names and package structure → attackers learn your architecture
 *   2. Line numbers → can be correlated with known CVEs or help craft exploits
 *   3. Library versions → identifies specific vulnerable dependencies
 *   4. Business logic paths → reveals conditional branches attackers can abuse
 *   5. Server technology → helps target framework-specific vulnerabilities
 * A generic 500 response discloses nothing while still signalling failure.
 * The real error is logged server-side where only authorised personnel can see it.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception server-side for debugging — never exposed to the client
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GlobalExceptionMapper", exception);

        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please contact the API administrator.");

        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
