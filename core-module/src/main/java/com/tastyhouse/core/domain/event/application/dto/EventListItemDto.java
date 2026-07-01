package com.tastyhouse.core.domain.event.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record EventListItemDto(
    Long eventId,
    String name,
    String thumbnailFilePath,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    @QueryProjection
    public EventListItemDto {
    }
}
