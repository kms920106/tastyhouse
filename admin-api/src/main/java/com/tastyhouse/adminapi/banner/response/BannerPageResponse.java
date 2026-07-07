package com.tastyhouse.adminapi.banner.response;

import java.util.List;

import com.tastyhouse.core.shared.page.PageResult;

public record BannerPageResponse(
    List<BannerListItemResponse> content,
    int page,
    int size,
    long totalElements
) {

    public static BannerPageResponse from(PageResult<BannerListItemResponse> pageResult) {
        return new BannerPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
