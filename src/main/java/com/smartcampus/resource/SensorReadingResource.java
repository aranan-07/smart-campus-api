package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Part 4 — Sensor Reading Sub-Resource
 *
 * Handles historical data for a specific sensor:
 *   GET  /api/v1/sensors/{sensorId}/readings       → get all readings
 *   POST /api/v1/sensors/{sensorId}/readings       → add a new reading
 *   GET  /api/v1/sensors/{sensorId}/readings/{rid} → get a specific reading
 *
 * This class is instantiated via the Sub-Resource Locator in SensorResource.
 * The sensorId is passed in at construction time, giving this class context
 * about which sensor it is managing without path parameter repetition.
 *
 * SIDE EFFECT: When a new reading is successfully POSTed, the parent Sensor's
 * currentValue is updated to reflect the latest measurement. This keeps data
 * consistent: a GET on /sensors/{id} always shows the most recent reading.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ── GET /api/v1/sensors/{sensorId}/readings ───────────────────────────────

    @GET
    public Response getAllReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        List<SensorReading> readings = DataStore.sensorReadings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    // ── POST /api/v1/sensors/{sensorId}/readings — Add a new reading ──────────

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Business Rule (Part 5.3): Cannot POST readings to a sensor in MAINTENANCE status
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept new readings."
            );
        }

        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is OFFLINE and cannot accept new readings."
            );
        }

        // Validate reading
        if (reading == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reading body is required with a 'value' field");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Auto-assign id and timestamp if not provided
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist the reading
        DataStore.sensorReadings
            .computeIfAbsent(sensorId, k -> new ArrayList<>())
            .add(reading);

        // ── SIDE EFFECT: Update parent sensor's currentValue ─────────────────
        // This ensures GET /sensors/{id} always returns the latest measurement
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    // ── GET /api/v1/sensors/{sensorId}/readings/{readingId} ──────────────────

    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        List<SensorReading> readings = DataStore.sensorReadings.getOrDefault(sensorId, new ArrayList<>());
        Optional<SensorReading> found = readings.stream()
            .filter(r -> r.getId().equals(readingId))
            .findFirst();

        if (found.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reading not found: " + readingId);
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(found.get()).build();
    }
}
