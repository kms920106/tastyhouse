package com.tastyhouse.core.domain.banner.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

public record BannerCreateCommand(
    BannerType type,
    String title,
    Long imageFileId,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {

    public static BannerCreateCommand of(
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        return new BannerCreateCommand(type, title, imageFileId, linkUrl, startDate, endDate, sort, visible);
    }
}
