package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Part 2 — Room Management Resource
 *
 * Manages the /api/v1/rooms collection.
 *
 * DESIGN NOTES:
 *
 * Full objects vs IDs only (Part 2 Q1):
 * Returning full objects is more convenient for clients (one round-trip) but costs
 * more bandwidth. Returning only IDs is lightweight but forces clients to make N
 * additional GET requests to fetch details (the "N+1 problem"). A hybrid approach —
 * returning minimal summaries (id + name) in the list and full details via GET /{id}
 * — is the industry standard (used by GitHub, Stripe, etc.). We return full objects
 * here for simplicity and clarity.
 *
 * DELETE idempotency (Part 2 Q2):
 * A DELETE is idempotent if repeated calls produce the same server state.
 * In our implementation:
 *   - 1st DELETE on an existing room → removes it, returns 204 No Content
 *   - 2nd DELETE on the same (now missing) room → returns 404 Not Found
 * The server state after each call is identical (room doesn't exist) but the
 * HTTP response code differs. Strict REST theory says idempotency refers to
 * server state, not status codes, so this IS idempotent. However, some teams
 * choose to return 204 on repeated deletes. Both approaches are valid.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // ── GET /api/v1/rooms — List all rooms ────────────────────────────────────

    @GET
    public Response getAllRooms() {
        Collection<Room> allRooms = DataStore.rooms.values();
        return Response.ok(allRooms).build();
    }

    // ── POST /api/v1/rooms — Create a new room ────────────────────────────────

    @POST
    public Response createRoom(Room room) {
        // Validate required fields
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room ID is required");
            error.put("status", "400");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (DataStore.rooms.containsKey(room.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room with ID '" + room.getId() + "' already exists");
            error.put("status", "409");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        // Ensure sensorIds list is initialised (never null)
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        DataStore.rooms.put(room.getId(), room);

        // 201 Created with the newly created room as the body
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // ── GET /api/v1/rooms/{roomId} — Fetch a single room ─────────────────────

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found: " + roomId);
            error.put("status", "404");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(room).build();
    }

    // ── DELETE /api/v1/rooms/{roomId} — Delete a room (with safety check) ─────

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        // 404 if room does not exist
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found: " + roomId);
            error.put("status", "404");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Business rule: cannot delete a room that still has sensors assigned
        if (!room.getSensorIds().isEmpty()) {
            // Throw custom exception — mapped to HTTP 409 by RoomNotEmptyExceptionMapper
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted because it still has " +
                room.getSensorIds().size() + " sensor(s) assigned: " + room.getSensorIds()
            );
        }

        DataStore.rooms.remove(roomId);

        // 204 No Content — deletion successful, no body needed
        return Response.noContent().build();
    }
}
