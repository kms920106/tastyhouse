package com.tastyhouse.core.entity.event.dto;

import java.time.LocalDateTime;

public record EventListItemDto(
    Long eventId,
    String name,
    String thumbnailFilePath,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
