package com.tastyhouse.core.domain.banner.application.dto;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;

public record BannerDetailDto(
    BannerId bannerId,
    BannerType type,
    String title,
    Long imageFileId,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static BannerDetailDto from(Banner banner) {
        return new BannerDetailDto(
            banner.getBannerId(),
            banner.getType(),
            banner.getTitle(),
            banner.getImageFileId(),
            banner.getLinkUrl(),
            banner.getStartDate(),
            banner.getEndDate(),
            banner.getSort(),
            banner.isVisible(),
            banner.getCreatedAt(),
            banner.getUpdatedAt()
        );
    }
}
