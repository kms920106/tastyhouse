package com.tastyhouse.application.banner.port.out;

import com.tastyhouse.domain.banner.model.BannerType;

public record BannerSearchCondition(
    BannerType type,
    String title,
    Boolean visible
) {

    public static BannerSearchCondition of(BannerType type, String title, Boolean visible) {
        return new BannerSearchCondition(type, title, visible);
    }
}
