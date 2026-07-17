package com.tastyhouse.core.domain.notice.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record NoticeListItemResult(
    Long id,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt
) {
    @QueryProjection
    public NoticeListItemResult {
    }
}
