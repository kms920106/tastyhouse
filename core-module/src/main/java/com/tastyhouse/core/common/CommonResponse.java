package com.tastyhouse.core.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private PageInfo pagination;

    @Getter
    @AllArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, null, data, null);
    }

    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, message, data, null);
    }

    public static <T> CommonResponse<List<T>> success(List<T> data, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        PageInfo pageInfo = new PageInfo(page, size, totalElements, totalPages);
        return new CommonResponse<>(true, null, data, pageInfo);
    }

    public static <T> CommonResponse<List<T>> success(List<T> data, int page, int size, long totalElements, String message) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        PageInfo pageInfo = new PageInfo(page, size, totalElements, totalPages);
        return new CommonResponse<>(true, message, data, pageInfo);
    }

    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>(false, message, null, null);
    }

    public static <T> CommonResponse<T> error(ApiError error) {
        return new CommonResponse<>(false, error.getMessage(), null, null);
    }
}
