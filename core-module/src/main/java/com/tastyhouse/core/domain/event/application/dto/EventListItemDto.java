package com.tastyhouse.core.domain.event.application.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

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
