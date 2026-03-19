package com.tastyhouse.core.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiError {
    private String code;
    private String message;

    public static ApiError of(String code, String message) {
        return new ApiError(code, message);
    }

    public static ApiError of(String message) {
        return new ApiError("INTERNAL_ERROR", message);
    }
}