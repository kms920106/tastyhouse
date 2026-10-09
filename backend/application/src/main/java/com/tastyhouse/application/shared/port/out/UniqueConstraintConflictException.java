package com.tastyhouse.application.shared.port.out;

public class UniqueConstraintConflictException extends RuntimeException {

    public UniqueConstraintConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
