package com.tastyhouse.webapi.banner.response;

public record BannerListItem(
    Long id,
    String title,
    String imageUrl,
    String linkUrl
) {
    public static BannerListItem from(
    Long id,
    String title,
    String imageUrl,
    String linkUrl
    ) {
    return new BannerListItem(
        id,
        title,
        imageUrl,
        linkUrl
    );
    }
}
