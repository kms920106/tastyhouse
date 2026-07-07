package com.tastyhouse.core.domain.banner.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

public record BannerUpdateCommand(
    BannerType type,
    String title,
    Long imageFileId,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {

    public static BannerUpdateCommand of(
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        return new BannerUpdateCommand(type, title, imageFileId, linkUrl, startDate, endDate, sort, visible);
    }
}
