package com.tastyhouse.application.banner.port.out;

public record BannerSearchCondition(
    String type,
    String title,
    Boolean visible
) {

    public static BannerSearchCondition of(String type, String title, Boolean visible) {
        return new BannerSearchCondition(type, title, visible);
    }
}
