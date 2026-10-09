package com.tastyhouse.infrastructure.mybatis.banner;

import java.time.LocalDateTime;

record BannerRow(
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
