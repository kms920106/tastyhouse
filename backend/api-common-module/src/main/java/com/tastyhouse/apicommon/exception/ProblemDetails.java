package com.tastyhouse.apicommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ProblemDetails {

    private ProblemDetails() {
    }

    public static ProblemDetail of(int statusCode, String errorCode, String message) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        if (errorCode != null) {
            problemDetail.setProperty("errorCode", errorCode);
        }
        return problemDetail;
    }
}
