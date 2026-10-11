package com.tastyhouse.application.banner.port.out;

public record BannerListItemResult(
    Long id,
    String title,
    String imageUrl,
    String linkUrl
) {
}
