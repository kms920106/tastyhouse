package com.tastyhouse.core.domain.banner.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BannerListItemDto(
    Long id,
    String title,
    String filePath,
    String linkUrl
) {
    @QueryProjection
    public BannerListItemDto {
    }
}
