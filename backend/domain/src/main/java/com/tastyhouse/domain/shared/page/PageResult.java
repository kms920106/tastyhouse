package com.tastyhouse.domain.shared.page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record PageResult<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int page,
    int size
) {

    public static <T> PageResult<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResult<>(content, totalElements, totalPages, page, size);
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), 0L, 0, page, size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedContent = content.stream()
            .map(mapper)
            .collect(Collectors.toList());
        return new PageResult<>(mappedContent, totalElements, totalPages, page, size);
    }
}
