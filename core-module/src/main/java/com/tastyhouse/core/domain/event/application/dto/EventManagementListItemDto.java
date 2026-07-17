package com.tastyhouse.core.domain.event.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.event.domain.model.EventStatus;

public record EventManagementListItemDto(
    Long id,
    String name,
    EventStatus status,
    Long thumbnailImageFileId,
    String thumbnailFileName,
    String thumbnailFilePath,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    @QueryProjection
    public EventManagementListItemDto {
    }
}
