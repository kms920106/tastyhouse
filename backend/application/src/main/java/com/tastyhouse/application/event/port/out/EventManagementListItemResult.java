package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.event.model.EventStatus;

public record EventManagementListItemResult(
    Long id,
    String name,
    EventStatus status,
    Long thumbnailImageFileId,
    String thumbnailFileName,
    String thumbnailUrl,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
