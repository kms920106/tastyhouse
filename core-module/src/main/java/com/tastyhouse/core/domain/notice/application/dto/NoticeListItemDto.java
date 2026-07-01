package com.tastyhouse.core.domain.notice.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

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
