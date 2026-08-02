package com.tastyhouse.apicommon.common;

import java.util.List;

public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final Pagination pagination;

    private ApiResponse(boolean success, String message, T data, Pagination pagination) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.pagination = pagination;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    public Pagination getPagination() {
        return this.pagination;
    }

    public static class Pagination {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;

        public Pagination(int page, int size, long totalElements, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getPage() {
            return this.page;
        }

        public int getSize() {
            return this.size;
        }

        public long getTotalElements() {
            return this.totalElements;
        }

        public int getTotalPages() {
            return this.totalPages;
        }
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            true,
            null,
            data,
            null
        );
    }

    public static <T> ApiResponse<List<T>> success(List<T> data, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        Pagination pageInfo = new Pagination(page, size, totalElements, totalPages);
        return new ApiResponse<>(
            true,
            null,
            data,
            pageInfo
        );
    }
}
