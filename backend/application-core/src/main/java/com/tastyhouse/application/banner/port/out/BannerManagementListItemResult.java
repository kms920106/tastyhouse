package com.tastyhouse.application.banner.port.out;

import java.time.LocalDateTime;

public record BannerManagementListItemResult(
    Long id,
    String type,
    String title,
    Long imageFileId,
    String imageFileName,
    String imageUrl,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {
}
