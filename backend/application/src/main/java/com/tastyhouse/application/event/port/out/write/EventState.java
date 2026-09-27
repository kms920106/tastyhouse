package com.tastyhouse.application.event.port.out.write;

import java.time.LocalDateTime;

public record EventState(
    Long id,
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    Long bannerImageFileId,
    String contentHtml,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
