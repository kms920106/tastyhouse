package com.tastyhouse.core.domain.banner.application.dto;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

public record BannerAdminSearchCondition(
    BannerType type,
    String title,
    Boolean visible
) {

    public static BannerAdminSearchCondition of(BannerType type, String title, Boolean visible) {
        return new BannerAdminSearchCondition(type, title, visible);
    }
}
