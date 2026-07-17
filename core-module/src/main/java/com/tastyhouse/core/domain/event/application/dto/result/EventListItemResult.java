package com.tastyhouse.core.domain.event.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record EventListItemResult(
    Long eventId,
    String name,
    String thumbnailFilePath,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    @QueryProjection
    public EventListItemResult {
    }
}
