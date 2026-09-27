package com.tastyhouse.application.banner.port.out.write;

import java.time.LocalDateTime;

public record BannerState(
    Long id,
    String type,
    String title,
    Long imageFileId,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
