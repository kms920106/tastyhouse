package com.tastyhouse.application.shared.exception;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(ApplicationErrorCodeSpec errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ApplicationErrorCodeSpec errorCode, String message) {
        super(errorCode, message);
    }
}
