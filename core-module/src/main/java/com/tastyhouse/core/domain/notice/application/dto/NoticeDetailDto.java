package com.tastyhouse.core.domain.notice.application.dto;

import java.time.LocalDateTime;

public record NoticeDetailDto(
    Long id,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NoticeDetailDto from(
        Long id,
        String title,
        String content,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new NoticeDetailDto(id, title, content, visible, createdAt, updatedAt);
    }
}
