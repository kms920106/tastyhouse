package com.tastyhouse.webapi.banner.request;

public record BannerListQuery(
    String title,
    Boolean active
) {
}
