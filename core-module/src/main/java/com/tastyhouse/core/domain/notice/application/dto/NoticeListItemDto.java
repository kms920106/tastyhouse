package com.tastyhouse.core.domain.notice.application.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record NoticeListItemDto(
    Long id,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt
) {
    @QueryProjection
    public NoticeListItemDto {
    }
}
