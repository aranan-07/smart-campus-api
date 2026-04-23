package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor Resource
 *
 * Manages the /api/v1/sensors collection and exposes a sub-resource locator
 * to delegate reading history to SensorReadingResource.
 *
 * DESIGN NOTES:
 *
 * @Consumes explanation (Part 3 Q1):
 * @Consumes(MediaType.APPLICATION_JSON) tells JAX-RS that this method ONLY accepts
 * requests with Content-Type: application/json. If a client sends text/plain or
 * application/xml, JAX-RS automatically returns HTTP 415 Unsupported Media Type
 * without ever calling our method. This acts as a contract enforcement layer,
 * preventing malformed or mistyped data from reaching business logic.
 *
 * @QueryParam vs @PathParam for filtering (Part 3 Q2):
 * Query parameters (?type=CO2) are semantically correct for filtering because:
 * 1. The resource being requested is still /sensors — the type is a filter, not a resource ID.
 * 2. Multiple filters can be combined: ?type=CO2&status=ACTIVE
 * 3. Filters are optional; path segments imply a required, specific resource.
 * 4. /sensors/type/CO2 implies "CO2" is a child resource of "type", which is incorrect.
 * REST convention: path = identity/hierarchy, query = filtering/searching/sorting.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // ── GET /api/v1/sensors[?type=X] — List sensors, optional type filter ─────

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = DataStore.sensors.values();

        if (type != null && !type.isBlank()) {
            // Filter by sensor type (case-insensitive)
            List<Sensor> filtered = all.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }

        return Response.ok(all).build();
    }

    // ── POST /api/v1/sensors — Register a new sensor ─────────────────────────

    @POST
    public Response createSensor(Sensor sensor) {
        // Validate required fields
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor ID is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (DataStore.sensors.containsKey(sensor.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor with ID '" + sensor.getId() + "' already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        // Validate that the referenced roomId actually exists
        String roomId = sensor.getRoomId();
        if (roomId == null || roomId.isBlank() || !DataStore.rooms.containsKey(roomId)) {
            // Throw custom exception — mapped to 422 by LinkedResourceNotFoundExceptionMapper
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor: Room '" + roomId + "' does not exist in the system."
            );
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        DataStore.sensors.put(sensor.getId(), sensor);

        // Initialise empty readings list for this sensor
        DataStore.sensorReadings.put(sensor.getId(), new ArrayList<>());

        // Update the room's sensorIds list (bidirectional link)
        Room room = DataStore.rooms.get(roomId);
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // ── GET /api/v1/sensors/{sensorId} — Get a single sensor ─────────────────

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(sensor).build();
    }

    // ── DELETE /api/v1/sensors/{sensorId} ────────────────────────────────────

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Remove sensor from its room's sensorIds list
        String roomId = sensor.getRoomId();
        if (roomId != null) {
            Room room = DataStore.rooms.get(roomId);
            if (room != null) {
                room.getSensorIds().remove(sensorId);
            }
        }

        DataStore.sensors.remove(sensorId);
        DataStore.sensorReadings.remove(sensorId);

        return Response.noContent().build();
    }

    // ── Sub-Resource Locator: /api/v1/sensors/{sensorId}/readings ────────────
    //
    // Part 4 — Sub-Resource Locator Pattern:
    // Instead of defining /readings endpoints here, we DELEGATE to a separate class.
    // This keeps SensorResource focused on sensor CRUD, and SensorReadingResource
    // focused on reading history. In large APIs, this pattern prevents a single
    // "god class" from growing to thousands of lines. JAX-RS instantiates
    // SensorReadingResource with the correct sensorId context, so each reading
    // operation already knows which sensor it belongs to.

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
