package com.tastyhouse.webapi.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        List<R> mappedContent = content.stream()
            .map(mapper)
            .collect(Collectors.toList());
        return new PageResponse<>(mappedContent, totalElements, totalPages, currentPage, pageSize);
    }
}
