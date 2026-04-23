package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException to HTTP 403 Forbidden.
 * A sensor in MAINTENANCE cannot physically accept new readings — the request is forbidden
 * by the current hardware state, not by authentication/authorisation.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("message", exception.getMessage());
        error.put("hint", "Change the sensor status to ACTIVE before submitting readings.");

        return Response
            .status(Response.Status.FORBIDDEN)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
