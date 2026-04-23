package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store for the Smart Campus API.
 *
 * WHY THIS CLASS EXISTS:
 * JAX-RS creates a new Resource instance per request by default.
 * If we stored Maps as instance fields on a Resource class, each request
 * would get a brand-new empty map — losing all previously stored data.
 *
 * Solution: Store all data in static ConcurrentHashMaps here.
 * ConcurrentHashMap is thread-safe, preventing race conditions when multiple
 * requests arrive simultaneously and try to read/write data at the same time.
 *
 * This is the recommended pattern for in-memory data management in JAX-RS
 * applications that do not use a database.
 */
public class DataStore {

    // Private constructor prevents instantiation — this is a utility class
    private DataStore() {}

    // ── Thread-safe in-memory collections ────────────────────────────────────

    /** All rooms, keyed by room ID */
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /** All sensors, keyed by sensor ID */
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * All sensor readings, keyed by sensor ID.
     * Each sensor has its own ordered list of readings.
     */
    public static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();


    // ── Seed data — pre-populate with realistic demo data ────────────────────

    static {
        // Seed Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("CS-101", "Computer Science Lab", 30);
        Room r3 = new Room("GYM-001", "Main Gymnasium", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Seed Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "CS-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors back to rooms
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());

        // Seed initial readings
        sensorReadings.put(s1.getId(), new ArrayList<>());
        sensorReadings.put(s2.getId(), new ArrayList<>());
        sensorReadings.put(s3.getId(), new ArrayList<>());
    }
}
