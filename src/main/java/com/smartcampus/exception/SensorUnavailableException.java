package com.smartcampus.exception;

/**
 * Thrown when a client attempts to POST a reading to a sensor in MAINTENANCE or OFFLINE status.
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
