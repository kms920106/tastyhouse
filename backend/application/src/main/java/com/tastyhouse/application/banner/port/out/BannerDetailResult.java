package com.tastyhouse.application.banner.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.banner.model.BannerType;

public record BannerDetailResult(
    Long id,
    BannerType type,
    String title,
    Long imageFileId,
    String imageFileName,
    String imageUrl,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
