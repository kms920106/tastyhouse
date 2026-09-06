package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.event.model.EventStatus;

public record EventManagementDetailResult(
    Long id,
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    String thumbnailFileName,
    String thumbnailUrl,
    Long bannerImageFileId,
    String bannerFileName,
    String bannerUrl,
    String contentHtml,
    EventStatus status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
