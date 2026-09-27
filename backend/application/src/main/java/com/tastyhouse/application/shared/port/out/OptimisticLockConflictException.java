package com.tastyhouse.application.shared.port.out;

public class OptimisticLockConflictException extends RuntimeException {
    public OptimisticLockConflictException(String message) {
        super(message);
    }

    public OptimisticLockConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
