package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

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
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
