package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 1 — Discovery Endpoint
 *
 * GET /api/v1
 * Returns API metadata: version, contact info, and links to all primary resource collections.
 *
 * HATEOAS (Hypermedia as the Engine of Application State):
 * By embedding navigable links in responses, clients can discover and traverse the API
 * dynamically — similar to how a browser follows hyperlinks on a webpage.
 * Client developers don't need to hardcode or memorize URIs; they start at the root
 * and follow the links. This decouples client code from server URL structure,
 * meaning the server can reorganize paths without breaking clients.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();

        // API metadata
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors");
        response.put("contact", "admin@smartcampus.ac.uk");
        response.put("status", "operational");

        // HATEOAS links — clients navigate the API from here
        Map<String, String> links = new HashMap<>();
        links.put("self",     "/api/v1");
        links.put("rooms",    "/api/v1/rooms");
        links.put("sensors",  "/api/v1/sensors");
        response.put("_links", links);

        // Resource collection descriptions
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms",   "Campus rooms and their sensor assignments");
        resources.put("sensors", "IoT sensors deployed across campus");
        response.put("resources", resources);

        return Response.ok(response).build();
    }
}
