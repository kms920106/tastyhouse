package com.tastyhouse.core.domain.banner.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

public record BannerAdminListItemDto(
    Long id,
    BannerType type,
    String title,
    Long imageFileId,
    String imageFileName,
    String imageFilePath,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {
    @QueryProjection
    public BannerAdminListItemDto {
    }
}
