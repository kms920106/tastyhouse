package com.tastyhouse.webapi.banner.response;

public record BannerListItemResponse(
    Long id,
    String title,
    String imageUrl,
    String linkUrl
) {
    public static BannerListItemResponse from(
    Long id,
    String title,
    String imageUrl,
    String linkUrl
    ) {
    return new BannerListItemResponse(
        id,
        title,
        imageUrl,
        linkUrl
    );
    }
}
