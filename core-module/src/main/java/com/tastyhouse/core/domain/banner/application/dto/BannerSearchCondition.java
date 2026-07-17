package com.tastyhouse.core.domain.banner.application.dto;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

public record BannerSearchCondition(
    BannerType type,
    String title,
    Boolean visible
) {

    public static BannerSearchCondition of(BannerType type, String title, Boolean visible) {
        return new BannerSearchCondition(type, title, visible);
    }
}
