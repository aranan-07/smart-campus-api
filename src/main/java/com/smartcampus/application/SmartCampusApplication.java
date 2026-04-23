package com.smartcampus.application;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application bootstrap class.
 *
 * The @ApplicationPath annotation sets the base URI for all REST resources.
 * This establishes "/api/v1" as the versioned entry point for the entire API.
 *
 * LIFECYCLE NOTE (answers Part 1 question):
 * By default, JAX-RS creates a NEW instance of each Resource class per HTTP request
 * (per-request lifecycle). This means resource classes are NOT singletons.
 * To safely share in-memory data across requests, we use a static DataStore
 * (a singleton utility class with ConcurrentHashMap) rather than instance fields.
 * Without this, each request would see an empty, freshly-created data store.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // ── Resource endpoints ──────────────────────────────────────────────
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // ── Exception mappers ────────────────────────────────────────────────
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // ── Filters ──────────────────────────────────────────────────────────
        classes.add(LoggingFilter.class);

        // ── Jackson JSON support ─────────────────────────────────────────────
        classes.add(JacksonFeature.class);

        return classes;
    }
}
