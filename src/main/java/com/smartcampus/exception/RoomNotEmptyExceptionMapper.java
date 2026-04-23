package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict.
 *
 * Returns a structured JSON error body explaining that the room still contains
 * active hardware and must be cleared before deletion.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 409);
        error.put("error", "Conflict");
        error.put("message", exception.getMessage());
        error.put("hint", "Reassign or delete all sensors in this room before attempting deletion.");

        return Response
            .status(Response.Status.CONFLICT)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
