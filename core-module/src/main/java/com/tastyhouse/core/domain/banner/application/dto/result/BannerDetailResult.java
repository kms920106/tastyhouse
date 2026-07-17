package com.tastyhouse.core.domain.banner.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;

public record BannerDetailResult(
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

    public static BannerDetailResult from(Banner banner) {
        return new BannerDetailResult(
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
