# Smart Campus Sensor & Room Management API
### 5COSC022W — Client-Server Architectures Coursework 2025/26

A production-quality RESTful API built with **JAX-RS (Jersey 2.39)** and an embedded **Grizzly HTTP server**. Manages campus rooms and IoT sensors with full CRUD, sub-resources, custom exception handling, and request/response logging.

---

## Table of Contents
1. [API Design Overview](#api-design-overview)
2. [Project Structure](#project-structure)
3. [Build & Run Instructions](#build--run-instructions)
4. [All Endpoints](#all-endpoints)
5. [Sample curl Commands](#sample-curl-commands)
6. [Conceptual Report — Question Answers](#conceptual-report--question-answers)

---

## API Design Overview

The API is versioned at `/api/v1` and follows REST resource hierarchy principles:

```
/api/v1                            ← Discovery endpoint (HATEOAS root)
/api/v1/rooms                      ← Room collection
/api/v1/rooms/{roomId}             ← Individual room
/api/v1/sensors                    ← Sensor collection (supports ?type= filter)
/api/v1/sensors/{sensorId}         ← Individual sensor
/api/v1/sensors/{sensorId}/readings        ← Reading history (sub-resource)
/api/v1/sensors/{sensorId}/readings/{rid}  ← Individual reading
```

**Technology Stack:**
- JAX-RS 2.1 (Jersey 2.39.1 implementation)
- Grizzly 2 embedded HTTP server (no WAR/Tomcat needed)
- Jackson 2.15 for JSON serialisation
- Java 11
- Maven (build tool)
- In-memory `ConcurrentHashMap` — no database

**Key Design Decisions:**
- All data is stored in a static `DataStore` class using `ConcurrentHashMap` to survive the per-request JAX-RS lifecycle
- Every error returns structured JSON — never a raw stack trace
- Exception Mappers handle all error conditions centrally
- A single logging filter covers all endpoints as a cross-cutting concern

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── application/
    │   ├── Main.java                        ← Entry point, starts Grizzly server
    │   ├── SmartCampusApplication.java      ← @ApplicationPath("/api/v1") JAX-RS bootstrap
    │   └── DataStore.java                   ← Singleton ConcurrentHashMap data store
    ├── model/
    │   ├── Room.java                        ← Room POJO
    │   ├── Sensor.java                      ← Sensor POJO
    │   └── SensorReading.java               ← SensorReading POJO
    ├── resource/
    │   ├── DiscoveryResource.java           ← GET /api/v1
    │   ├── RoomResource.java                ← GET/POST/DELETE /api/v1/rooms
    │   ├── SensorResource.java              ← GET/POST/DELETE /api/v1/sensors + sub-resource locator
    │   └── SensorReadingResource.java       ← GET/POST /api/v1/sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotEmptyException.java       ← Custom exception
    │   ├── RoomNotEmptyExceptionMapper.java ← → HTTP 409
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java ← → HTTP 422
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java      ← → HTTP 403
    │   └── GlobalExceptionMapper.java       ← Catch-all → HTTP 500
    └── filter/
        └── LoggingFilter.java               ← Request + Response logging
```

---

## Build & Run Instructions

### Prerequisites
- Java 11 or higher (`java -version`)
- Apache Maven 3.6+ (`mvn -version`)

### Step 1 — Clone the repository
```bash
git clone https://github.com/aranan-07/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the fat JAR
```bash
mvn clean package
```
This produces `target/smart-campus-api-1.0-SNAPSHOT.jar` — a self-contained JAR with all dependencies bundled via the Maven Shade Plugin.

### Step 3 — Run the server
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

### Step 4 — Verify it is running
```bash
curl http://localhost:8080/api/v1
```
You should see JSON with API metadata and links.

> The server listens on **port 8080** by default. Press `CTRL+C` to stop.

---

## All Endpoints

| Method | Path | Description | Success Code |
|--------|------|-------------|-------------|
| GET | /api/v1 | Discovery / HATEOAS root | 200 |
| GET | /api/v1/rooms | List all rooms | 200 |
| POST | /api/v1/rooms | Create a room | 201 |
| GET | /api/v1/rooms/{roomId} | Get room by ID | 200 |
| DELETE | /api/v1/rooms/{roomId} | Delete a room (fails if sensors exist) | 204 |
| GET | /api/v1/sensors | List all sensors (optional `?type=`) | 200 |
| POST | /api/v1/sensors | Register a sensor | 201 |
| GET | /api/v1/sensors/{sensorId} | Get sensor by ID | 200 |
| DELETE | /api/v1/sensors/{sensorId} | Delete a sensor | 204 |
| GET | /api/v1/sensors/{sensorId}/readings | Get all readings for sensor | 200 |
| POST | /api/v1/sensors/{sensorId}/readings | Add a new reading | 201 |
| GET | /api/v1/sensors/{sensorId}/readings/{rid} | Get a specific reading | 200 |

---

## Sample curl Commands

### 1. Discovery — GET /api/v1
```bash
curl -X GET http://localhost:8080/api/v1 \
     -H "Accept: application/json"
```

### 2. Create a Room — POST /api/v1/rooms
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{
           "id": "LAB-202",
           "name": "Physics Lab",
           "capacity": 40
         }'
```

### 3. Register a Sensor linked to a Room — POST /api/v1/sensors
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{
           "id": "TEMP-002",
           "type": "Temperature",
           "status": "ACTIVE",
           "currentValue": 21.0,
           "roomId": "LAB-202"
         }'
```

### 4. Filter Sensors by type — GET /api/v1/sensors?type=CO2
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
     -H "Accept: application/json"
```

### 5. Post a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 23.7}'
```

### 6. Get all readings for a sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-002/readings \
     -H "Accept: application/json"
```

### 7. Try to delete a room with sensors (triggers 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
# Returns: 409 Conflict — room still has sensors
```

### 8. Try to add a reading to a MAINTENANCE sensor (triggers 403)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 15}'
# Returns: 403 Forbidden — sensor is in MAINTENANCE
```

### 9. Register a sensor with a non-existent roomId (triggers 422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{
           "id": "TEMP-999",
           "type": "Temperature",
           "status": "ACTIVE",
           "currentValue": 0.0,
           "roomId": "DOES-NOT-EXIST"
         }'
# Returns: 422 Unprocessable Entity
```

### 10. Delete a sensor, then delete the now-empty room
```bash
# First delete the sensor
curl -X DELETE http://localhost:8080/api/v1/sensors/TEMP-002

# Then the room can be deleted safely
curl -X DELETE http://localhost:8080/api/v1/rooms/LAB-202
# Returns: 204 No Content
```

---

## Conceptual Report — Question Answers

---

### Part 1.1 — JAX-RS Resource Lifecycle & In-Memory Data Management

**Default JAX-RS Resource Lifecycle:**

By default, JAX-RS creates a **new instance of each Resource class for every incoming HTTP request** (per-request lifecycle). The runtime does NOT treat resource classes as singletons. This means if you store data as instance fields (e.g., `private Map<String, Room> rooms = new HashMap<>()`) inside a resource class, every request will see a brand-new, empty map — discarding all previously stored data the moment the request ends.

**Impact on In-Memory Data Structures:**

To correctly persist data across requests without a database, data must be stored in a location that outlives any single request instance. This project uses a dedicated `DataStore` utility class with `static final ConcurrentHashMap` fields. Being `static`, these maps are created once when the JVM loads the class and exist for the entire lifetime of the server process. Being `ConcurrentHashMap` (rather than plain `HashMap`), they are thread-safe — multiple simultaneous requests can read and write without causing `ConcurrentModificationException` or data corruption.

If a plain `HashMap` were used instead, two simultaneous POST requests could corrupt the map's internal structure, causing data loss or infinite loops — a race condition. The static + `ConcurrentHashMap` pattern is the standard in-memory solution for JAX-RS applications that do not use a database.

---

### Part 1.2 — HATEOAS and Hypermedia in RESTful APIs

**What is HATEOAS?**

HATEOAS (Hypermedia as the Engine of Application State) is a constraint of REST architecture where API responses include navigable links to related resources and available actions. Just as a web browser user never manually types sub-page URLs — they click links — an API client using HATEOAS starts at a single root URL and discovers all other endpoints from the links embedded in responses.

**Benefits over static documentation:**

Static documentation becomes stale as APIs evolve, and client code hardcoded against specific URLs breaks when those URLs change. With HATEOAS, clients are decoupled from URL structure — they follow links dynamically. This reduces the risk of breaking changes, eliminates the need to coordinate URL updates between server and client teams, and allows APIs to evolve without client-side code changes. For example, our discovery endpoint at `GET /api/v1` returns a `_links` object with `rooms → /api/v1/rooms` and `sensors → /api/v1/sensors`. A client implementation that starts here and follows links will automatically adapt if paths change.

---

### Part 2.1 — Full Objects vs IDs in List Responses

Returning only IDs in a list response is extremely lightweight (minimal bandwidth), but forces clients to make one additional `GET /{id}` request per item to retrieve useful data — known as the **N+1 problem**. For a list of 500 rooms, this means 501 HTTP round-trips, each with latency overhead.

Returning full objects gives clients everything in one response, eliminating extra requests and simplifying client code. The trade-off is higher bandwidth per response. The industry standard compromise is to return **minimal summaries** in list responses (e.g., `id`, `name`, `capacity`) and reserve the full object for `GET /{id}`. This project returns full objects for simplicity, which is appropriate for small datasets and straightforward client consumption.

---

### Part 2.2 — Is DELETE Idempotent in This Implementation?

**Yes**, the DELETE operation is idempotent in terms of server state. Idempotency in REST means that making the same request multiple times produces the same server state as making it once.

- **First DELETE** on an existing room (with no sensors): removes the room, returns `204 No Content`.
- **Second DELETE** on the same (now non-existent) room: the room is still gone, returns `404 Not Found`.

After both calls, the server state is identical — the room does not exist. The HTTP *status code* differs between the first and second call, but idempotency is defined in terms of **server state**, not response codes. This is the standard interpretation (RFC 9110). Some teams prefer to return `204` on repeated deletes (treating "already deleted" as a no-op), which is also a valid and idempotent design.

---

### Part 3.1 — Consequences of Mismatched Content-Type with @Consumes

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares a **contract**: this method only accepts request bodies with `Content-Type: application/json`. If a client sends `text/plain` or `application/xml`, JAX-RS intercepts the request **before it reaches our method** and automatically returns **HTTP 415 Unsupported Media Type**. Our business logic code is never invoked.

This is a critical safeguard: without it, JAX-RS would attempt to deserialise (e.g.) a plain text string into a `Sensor` Java object, almost certainly throwing a `JsonParseException` or `NullPointerException`. The annotation acts as a firewall, ensuring only structurally valid JSON ever reaches our deserialisation and validation logic.

---

### Part 3.2 — @QueryParam vs @PathParam for Filtering

Using a query parameter (`GET /api/v1/sensors?type=CO2`) is semantically superior to a path segment (`GET /api/v1/sensors/type/CO2`) for filtering for the following reasons:

1. **Semantic correctness**: Path segments identify *resources*. `/sensors/CO2` implies "CO2" is a specific resource within "sensors", which is incorrect — `CO2` is a *filter criterion*, not a resource identity.
2. **Optionality**: Query parameters are naturally optional. A path segment is mandatory — you'd need two separate `@GET` methods to support both `/sensors` and `/sensors/type/CO2`.
3. **Composability**: Multiple filters are trivially combined: `?type=CO2&status=ACTIVE`. Path-based filtering requires increasingly complex and unintuitive URL designs (e.g., `/sensors/type/CO2/status/ACTIVE`).
4. **REST convention**: The REST standard uses path for hierarchy/identity and query string for searching, filtering, sorting, and pagination. Following this convention makes the API predictable and immediately understandable to other developers.

---

### Part 4.1 — Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern delegates the handling of a nested path to a separate class. In `SensorResource`, the method `@Path("/{sensorId}/readings")` returns an instance of `SensorReadingResource` rather than defining reading endpoints inline.

**Architectural benefits:**

1. **Single Responsibility**: `SensorResource` handles sensor CRUD; `SensorReadingResource` handles reading history. Each class has one clear purpose.
2. **Manageability**: In large APIs, defining every nested path in one class creates unmaintainable "god classes" with thousands of lines. Delegation keeps each file focused and short.
3. **Context injection**: The `sensorId` is passed to `SensorReadingResource` at construction time. Every method in that class already knows its sensor context without repeating `@PathParam` on every method.
4. **Testability**: Each sub-resource class can be unit tested in isolation, without needing to set up the full parent resource.
5. **Reusability**: The same sub-resource class could be composed from multiple parent paths if the API structure requires it.

---

### Part 5.2 — Why HTTP 422 is More Accurate Than 404 for Missing Referenced Resources

**HTTP 404 Not Found** means the *URL itself* was not found on the server. The resource identified by the request URI does not exist.

**HTTP 422 Unprocessable Entity** means the server *understood the request*, the URL is valid, the JSON is syntactically correct, but the *semantic content is logically invalid* — the server cannot process it because of a business rule violation or referential integrity problem.

When a client POSTs a new sensor with `"roomId": "FAKE-999"`, the URL `/api/v1/sensors` is found (200-level routing succeeds). The JSON body is valid. But the *value* of `roomId` references a room that doesn't exist. This is a semantic failure — the referenced dependency is missing inside an otherwise valid payload. Returning 404 would falsely imply that `/api/v1/sensors` itself wasn't found, which is misleading. HTTP 422 correctly communicates: "your request arrived and was understood, but I cannot fulfil it because of a logical problem within the body." This distinction helps API clients implement better error handling, as they can distinguish routing errors from data integrity errors.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing Java stack traces to external API consumers represents a significant security vulnerability for several reasons:

1. **Architecture disclosure**: Full class names and package paths (e.g., `com.smartcampus.resource.SensorResource.createSensor(SensorResource.java:67)`) reveal the internal code structure, making it easier to craft targeted exploits.
2. **Dependency fingerprinting**: Stack traces often include third-party library names and versions (e.g., Jersey, Jackson). Attackers can cross-reference these against public CVE databases to identify known vulnerabilities.
3. **Logic path revelation**: Exception messages and call stacks reveal conditional branches and validation logic, helping attackers understand what inputs trigger which code paths.
4. **Line number correlation**: Exact line numbers in source code can be matched with code leaked through other vectors (e.g., a public GitHub repo) to understand precise exploit targets.
5. **Technology identification**: Framework-specific exceptions immediately identify the server technology stack, narrowing the attack surface for known framework exploits.

The correct approach — implemented in our `GlobalExceptionMapper` — is to log the full exception server-side (where only authorised personnel can view it) and return only a generic `"An unexpected error occurred"` message to external consumers.

---

### Part 5.5 — Why Use JAX-RS Filters Instead of Manual Logging

Inserting `Logger.info()` statements manually into every resource method creates multiple problems:

1. **Code duplication**: With 12 endpoints, logging must be written and maintained in 12 places. Adding a new field to log (e.g., request latency) requires editing every method.
2. **Inconsistency**: Developers can forget to add logging to new methods, creating gaps in observability.
3. **Pollution of business logic**: Resource methods should express business rules, not infrastructure concerns. Mixing logging into them violates the Single Responsibility Principle.
4. **Inflexibility**: Changing log format, log level, or adding structured logging (e.g., JSON logs for Kibana) requires touching every method.

JAX-RS filters implement the **cross-cutting concern** pattern: one class intercepts every request and response automatically, regardless of which resource method handles it. New endpoints are automatically covered. The logging implementation is in one place, making it trivial to change. This is the same principle behind Spring AOP, servlet filters, and middleware in Node.js — infrastructure concerns belong at the framework boundary, not inside business logic.
