package com.tastyhouse.core.entity.notice.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record NoticeListItemDto(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {
    @QueryProjection
    public NoticeListItemDto {
    }
}
