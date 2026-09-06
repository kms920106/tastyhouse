package com.tastyhouse.domain.exception;

public interface ErrorCodeSpec {
    int getHttpStatusCode();

    String getCode();

    String getDefaultMessage();
}
