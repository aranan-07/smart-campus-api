package com.smartcampus.exception;

/**
 * Thrown when a client references a resource (e.g., a roomId) that does not exist,
 * but the request itself is structurally valid JSON.
 *
 * WHY 422 and not 404? (Part 5.2 question answer):
 * HTTP 404 means "the URL you requested was not found on this server."
 * But here, the URL /api/v1/sensors is perfectly valid — we found it.
 * The problem is inside the request body: the referenced roomId doesn't exist.
 * HTTP 422 Unprocessable Entity means "the request was well-formed (valid JSON,
 * correct Content-Type) but the semantic content is invalid — the server
 * understands what you're asking but cannot process it due to a logical error."
 * This is semantically far more accurate and helps clients distinguish between
 * "wrong URL" (404) and "logically invalid request body" (422).
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
