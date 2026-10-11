package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

public record EventManagementListItemResult(
    Long id,
    String name,
    String status,
    Long thumbnailImageFileId,
    String thumbnailFileName,
    String thumbnailUrl,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
