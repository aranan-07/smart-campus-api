package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 * Used when a valid JSON request references a resource (e.g., roomId) that doesn't exist.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", exception.getMessage());
        error.put("hint", "Ensure all referenced resource IDs (e.g., roomId) exist before creating dependent resources.");

        return Response
            .status(422) // 422 is not in Response.Status enum in older JAX-RS versions
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
